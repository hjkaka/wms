package com.example.wms_backend.service.impl;

import com.example.wms_backend.constant.StockTakeStatus;
import com.example.wms_backend.dto.StockTakeCreateDTO;
import com.example.wms_backend.dto.StockTakeItemUpdateDTO;
import com.example.wms_backend.dto.StockTakePageDTO;
import com.example.wms_backend.entity.Stock;
import com.example.wms_backend.entity.StockLog;
import com.example.wms_backend.entity.StockTakeItem;
import com.example.wms_backend.entity.StockTakeOrder;
import com.example.wms_backend.mapper.StockLogMapper;
import com.example.wms_backend.mapper.StockMapper;
import com.example.wms_backend.mapper.StockTakeItemMapper;
import com.example.wms_backend.mapper.StockTakeOrderMapper;
import com.example.wms_backend.service.StockTakeService;
import com.example.wms_backend.vo.StockTakeItemVO;
import com.example.wms_backend.vo.StockTakeOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 盘点业务实现（s3-3）
 * 简化流程：草稿(0) → 直接过账(1)
 * - 创建：自动纳入所选仓库所有有库存商品作为账面快照亮细
 * - 过账：实盘≠账面 生成盈亏流水（盘盈 CHECK_IN / 盘亏 CHECK_OUT）并调整库存
 * 全程 @Transactional：任一明细调整失败整体回滚，保证库存与流水一致。
 */
@Service
@Transactional
public class StockTakeServiceImpl implements StockTakeService {

    @Autowired
    private StockTakeOrderMapper stockTakeOrderMapper;
    @Autowired
    private StockTakeItemMapper stockTakeItemMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private StockLogMapper stockLogMapper;

    /** 获取当前登录用户ID（从 SecurityContext 取出） */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new RuntimeException("获取当前用户失败，请检查登录状态");
    }

    @Override
    public StockTakeOrder create(StockTakeCreateDTO dto) {
        if (dto.getWarehouseId() == null) {
            throw new RuntimeException("请选择盘点仓库");
        }

        // ===== 1. 取出所选仓库的库存快照（有库存的商品）作为盘点账面明细 =====
        List<StockTakeItem> snapshot = stockTakeItemMapper.getStockSnapshot(dto.getWarehouseId());
        if (snapshot == null || snapshot.isEmpty()) {
            throw new RuntimeException("该仓库暂无库存商品，无需盘点");
        }

        // ===== 2. 生成盘点单号 + 建主表 =====
        StockTakeOrder order = new StockTakeOrder();
        order.setOrderNo(generateOrderNo());
        order.setWarehouseId(dto.getWarehouseId());
        order.setStatus(StockTakeStatus.DRAFT);
        order.setSnapshotTime(LocalDateTime.now());
        order.setRemark(dto.getRemark());
        order.setOperatorId(getCurrentUserId());
        stockTakeOrderMapper.insert(order);

        // ===== 3. 生成盘点明细（账面=快照数量，实盘未录入=null） =====
        List<StockTakeItem> items = new ArrayList<>();
        for (StockTakeItem s : snapshot) {
            StockTakeItem it = new StockTakeItem();
            it.setOrderId(order.getId());
            it.setProductId(s.getProductId());
            it.setSystemQuantity(s.getSystemQuantity());
            items.add(it);
        }
        stockTakeItemMapper.batchInsert(items);

        return order;
    }

    @Override
    public StockTakeOrderVO getOrder(Long id) {
        StockTakeOrderVO vo = stockTakeOrderMapper.findVOById(id);
        if (vo == null) {
            throw new RuntimeException("盘点单不存在，id=" + id);
        }
        return vo;
    }

    @Override
    public List<StockTakeItemVO> getItems(Long id) {
        return stockTakeItemMapper.findByOrderId(id);
    }

    @Override
    public void updateItems(Long id, List<StockTakeItemUpdateDTO> items) {
        StockTakeOrder order = requireDraftOrder(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("盘点明细不能为空");
        }
        for (StockTakeItemUpdateDTO dto : items) {
            if (dto.getId() == null) {
                throw new RuntimeException("盘点明细ID不能为空");
            }
            stockTakeItemMapper.updateCounted(dto.getId(), dto.getCountedQuantity(), dto.getRemark());
        }
    }

    @Override
    public StockTakeOrder post(Long id) {
        StockTakeOrder order = requireDraftOrder(id);
        Long warehouseId = order.getWarehouseId();
        String orderNo = order.getOrderNo();
        Long operatorId = getCurrentUserId();

        List<StockTakeItem> items = stockTakeItemMapper.findEntitiesByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("盘点单明细为空，无法过账");
        }

        // ===== 逐条明细：实盘≠账面 → 调整库存 + 写盈亏流水 =====
        for (StockTakeItem item : items) {
            if (item.getCountedQuantity() == null) {
                continue; // 未录入实盘的商品不调整
            }
            int system = item.getSystemQuantity() == null ? 0 : item.getSystemQuantity();
            int counted = item.getCountedQuantity();
            int variance = counted - system; // 正=盘盈，负=盘亏
            if (variance == 0) {
                continue;
            }

            Long productId = item.getProductId();
            Stock stock = stockMapper.findByProductAndWarehouse(productId, warehouseId);
            int beforeQty = (stock == null) ? 0 : stock.getQuantity();

            if (variance > 0) {
                // 盘盈：增加库存
                stockMapper.increaseQuantity(productId, warehouseId, variance);
            } else {
                // 盘亏：扣减库存（带防负）
                int affected = stockMapper.decreaseQuantity(productId, warehouseId, -variance);
                if (affected == 0) {
                    throw new RuntimeException(
                            "商品ID " + productId + " 库存不足，无法盘亏（当前库存 " + beforeQty + ", 需扣减 " + (-variance) + "）");
                }
            }

            // 写盈亏流水：盘盈 CHECK_IN / 盘亏 CHECK_OUT
            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(warehouseId);
            log.setChangeType(variance > 0 ? "CHECK_IN" : "CHECK_OUT");
            log.setChangeQuantity(Math.abs(variance));
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty + variance);
            log.setOrderNo(orderNo);
            log.setOperatorId(operatorId);
            stockLogMapper.insert(log);
        }

        // 更新状态为已过账
        stockTakeOrderMapper.updateStatus(id, StockTakeStatus.POSTED);
        return order;
    }

    @Override
    public Map<String, Object> getPage(StockTakePageDTO dto) {
        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<StockTakeOrderVO> list = stockTakeOrderMapper.searchPage(dto, offset);
        long total = stockTakeOrderMapper.countSearch(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());
        return result;
    }

    /** 校验盘点单存在且为草稿状态 */
    private StockTakeOrder requireDraftOrder(Long id) {
        StockTakeOrder order = stockTakeOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("盘点单不存在，id=" + id);
        }
        if (order.getStatus() != StockTakeStatus.DRAFT) {
            throw new RuntimeException("盘点单已过账，不可再操作");
        }
        return order;
    }

    /** 生成盘点单号：PD + yyyyMMdd + 3位流水 */
    private String generateOrderNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PD" + today;
        String maxOrderNo = stockTakeOrderMapper.getMaxOrderNoByDate(prefix);
        int seq = 1;
        if (maxOrderNo != null && !maxOrderNo.isEmpty()) {
            seq = Integer.parseInt(maxOrderNo.substring(maxOrderNo.length() - 3)) + 1;
        }
        return prefix + String.format("%03d", seq);
    }
}