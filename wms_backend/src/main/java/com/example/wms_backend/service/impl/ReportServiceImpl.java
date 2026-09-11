package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.ReportQueryDTO;
import com.example.wms_backend.mapper.ReportMapper;
import com.example.wms_backend.service.ReportService;
import com.example.wms_backend.vo.AbcVO;
import com.example.wms_backend.vo.DormantVO;
import com.example.wms_backend.vo.PeriodAggVO;
import com.example.wms_backend.vo.TurnoverVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表实现（s3-5）
 * 聚合 SQL 在 ReportMapper，差值/占比/周转等在 Java 计算
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<TurnoverVO> turnover(ReportQueryDTO dto) {
        List<PeriodAggVO> aggList = reportMapper.periodAgg(dto.getStartDate(), dto.getEndDate());
        Map<Long, Integer> stockMap = currentStockMap();
        int periodDays = periodDays(dto.getStartDate(), dto.getEndDate());

        List<TurnoverVO> result = new ArrayList<>();
        for (PeriodAggVO a : aggList) {
            int outQty = a.getOutQuantity() == null ? 0 : a.getOutQuantity();
            int inQty = a.getInQuantity() == null ? 0 : a.getInQuantity();
            int endQty = stockMap.getOrDefault(a.getProductId(), 0);
            // 期初 = 期末 - 期间净变动
            int beginQty = endQty - (inQty - outQty);
            double avg = (beginQty + endQty) / 2.0;
            double times = avg > 0 ? outQty / avg : 0;
            // 周转天数 = 期间天数 / 周转次数
            Double days = times > 0 ? round(periodDays / times) : null;
            BigDecimal price = a.getPrice() == null ? BigDecimal.ZERO : a.getPrice();

            TurnoverVO vo = new TurnoverVO();
            vo.setProductId(a.getProductId());
            vo.setProductName(a.getProductName());
            vo.setProductCode(a.getProductCode());
            vo.setOutQuantity(outQty);
            vo.setOutAmount(price.multiply(BigDecimal.valueOf(outQty)));
            vo.setBeginQuantity(beginQty);
            vo.setEndQuantity(endQty);
            vo.setAvgQuantity(round(avg));
            vo.setTurnoverTimes(round(times));
            vo.setTurnoverDays(days);
            result.add(vo);
        }
        // 周转次数升序在前（转得慢的更值得关注）
        result.sort((x, y) -> Double.compare(
                x.getTurnoverTimes() == null ? 0 : x.getTurnoverTimes(),
                y.getTurnoverTimes() == null ? 0 : y.getTurnoverTimes()));
        return result;
    }

    @Override
    public List<DormantVO> dormant(ReportQueryDTO dto) {
        int days = dto.getDays() == null || dto.getDays() <= 0 ? 30 : dto.getDays();
        List<PeriodAggVO> aggList = reportMapper.periodAgg(null, null); // 呆滞不限定期间，看全量动销
        Map<Long, Integer> stockMap = currentStockMap();
        Map<Long, LocalDateTime> lastOutMap = lastOutMap();
        LocalDate today = LocalDate.now();

        List<DormantVO> result = new ArrayList<>();
        for (PeriodAggVO a : aggList) {
            Integer qty = stockMap.get(a.getProductId());
            if (qty == null || qty <= 0) continue; // 只统计仍有库存的商品
            LocalDateTime ref = lastOutMap.get(a.getProductId());
            if (ref == null) ref = a.getCreateTime(); // 从未出库：以创建时间兜底
            long dormantDays = ChronoUnit.DAYS.between(
                    ref == null ? today : ref.toLocalDate(), today);
            if (dormantDays < days) continue; // 未达阈值不算呆滞

            DormantVO vo = new DormantVO();
            vo.setProductId(a.getProductId());
            vo.setProductName(a.getProductName());
            vo.setProductCode(a.getProductCode());
            vo.setQuantity(qty);
            vo.setAmount((a.getPrice() == null ? BigDecimal.ZERO : a.getPrice()).multiply(BigDecimal.valueOf(qty)));
            vo.setLastOutDate(ref);
            vo.setDormantDays((int) dormantDays);
            result.add(vo);
        }
        result.sort((x, y) -> Integer.compare(y.getDormantDays(), x.getDormantDays())); // 呆滞最久在前
        return result;
    }

    @Override
    public List<AbcVO> abc(ReportQueryDTO dto) {
        List<PeriodAggVO> aggList = reportMapper.periodAgg(dto.getStartDate(), dto.getEndDate());
        // 组装 (outQty, outAmount) 并按金额降序
        List<AbcVO> list = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PeriodAggVO a : aggList) {
            int outQty = a.getOutQuantity() == null ? 0 : a.getOutQuantity();
            BigDecimal amount = (a.getPrice() == null ? BigDecimal.ZERO : a.getPrice())
                    .multiply(BigDecimal.valueOf(outQty));
            total = total.add(amount);
            AbcVO vo = new AbcVO();
            vo.setProductId(a.getProductId());
            vo.setProductName(a.getProductName());
            vo.setProductCode(a.getProductCode());
            vo.setOutQuantity(outQty);
            vo.setOutAmount(amount);
            list.add(vo);
        }
        list.sort((x, y) -> y.getOutAmount().compareTo(x.getOutAmount()));

        // 累计占比 + 分类
        BigDecimal run = BigDecimal.ZERO;
        for (AbcVO vo : list) {
            run = run.add(vo.getOutAmount());
            double ratio = total.signum() > 0
                    ? vo.getOutAmount().divide(total, 6, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0;
            double cum = total.signum() > 0
                    ? run.divide(total, 6, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0;
            vo.setAmountRatio(round(ratio));
            vo.setCumulativeRatio(round(cum));
            vo.setAbcClass(cum <= 70 ? "A" : (cum <= 90 ? "B" : "C"));
        }
        return list;
    }

    // ---- 工具 ----

    private Map<Long, Integer> currentStockMap() {
        Map<Long, Integer> map = new HashMap<>();
        for (Map<String, Object> r : reportMapper.currentStockQuantity()) {
            Number pid = (Number) r.get("product_id");
            Number qty = (Number) r.get("quantity");
            if (pid != null && qty != null) map.put(pid.longValue(), qty.intValue());
        }
        return map;
    }

    private Map<Long, LocalDateTime> lastOutMap() {
        Map<Long, LocalDateTime> map = new HashMap<>();
        for (Map<String, Object> r : reportMapper.lastOutDate()) {
            Number pid = (Number) r.get("product_id");
            Object lo = r.get("last_out");
            if (pid != null && lo != null) {
                if (lo instanceof java.sql.Timestamp) {
                    map.put(pid.longValue(), ((java.sql.Timestamp) lo).toLocalDateTime());
                } else if (lo instanceof LocalDateTime) {
                    map.put(pid.longValue(), (LocalDateTime) lo);
                }
            }
        }
        return map;
    }

    private int periodDays(String start, String end) {
        try {
            LocalDate s = start == null || start.isEmpty() ? LocalDate.now().minusDays(30) : LocalDate.parse(start);
            LocalDate e = end == null || end.isEmpty() ? LocalDate.now() : LocalDate.parse(end);
            if (e.isBefore(s)) return 1;
            long d = ChronoUnit.DAYS.between(s, e) + 1;
            return (int) Math.max(1, d);
        } catch (Exception ex) {
            return 30;
        }
    }

    private Double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}