package com.example.wms_backend.controller;

import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.StockQueryDTO;
import com.example.wms_backend.service.StockService;
import com.example.wms_backend.vo.StockInfoVO;
import com.example.wms_backend.vo.StockSummaryVO;
import com.example.wms_backend.vo.StockTrendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    // GET /api/stock/page?keyword=&warehouseId=&pageNum=&pageSize=
    @GetMapping("/page")
    public Result<Map<String, Object>> page(StockQueryDTO queryDTO) {
        return Result.success(stockService.queryStockPage(queryDTO));
    }

    // GET /api/stock/summary  → 按商品汇总
    @GetMapping("/summary")
    public Result<List<StockSummaryVO>> summaryByProduct() {
        return Result.success(stockService.summaryByProduct());
    }

    // GET /api/stock/trend?startDate=2026-08-01&endDate=2026-08-21  → 出入库日报
    @GetMapping("/trend")
    public Result<List<StockTrendVO>> trend(@RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate) {
        return Result.success(stockService.queryTrend(startDate, endDate));
    }
}