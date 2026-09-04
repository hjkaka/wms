package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.StockQueryDTO;
import com.example.wms_backend.mapper.StockLogMapper;
import com.example.wms_backend.mapper.StockMapper;
import com.example.wms_backend.service.StockService;
import com.example.wms_backend.vo.StockInfoVO;
import com.example.wms_backend.vo.StockSummaryVO;
import com.example.wms_backend.vo.StockTrendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // 交给 Spring 管理成 Bean
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    public Map<String, Object> queryStockPage(StockQueryDTO dto) {
        // 1. 换算偏移量：第1页跳0条、第2页跳pageSize条... (pageNum-1)*pageSize
        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        // 2. 查当前页数据
        List<StockInfoVO> list = stockMapper.searchPage(dto, offset);
        // 3. 查总条数（用于前端算总页数）
        long total = stockMapper.countSearch(dto);
        // 4. 组装分页结果（和商品分页同套路）
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());
        return result;
    }

    @Override
    public List<StockSummaryVO> summaryByProduct() {
        // 聚合逻辑全在 SQL，Java 只转发
        return stockMapper.summaryByProduct();
    }

    @Override
    public List<StockSummaryVO> summaryByWarehouse() {
        return stockMapper.summaryByWarehouse();
    }

    @Override
    public List<StockTrendVO> queryTrend(String startDate, String endDate) {
        // 直接把入参转发给 Mapper，聚合逻辑全在 SQL
        return stockLogMapper.trendDaily(startDate, endDate);
    }
}