package com.example.wms_backend.controller;

import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.StockTakeCreateDTO;
import com.example.wms_backend.dto.StockTakeItemUpdateDTO;
import com.example.wms_backend.dto.StockTakePageDTO;
import com.example.wms_backend.entity.StockTakeOrder;
import com.example.wms_backend.service.StockTakeService;
import com.example.wms_backend.vo.StockTakeItemVO;
import com.example.wms_backend.vo.StockTakeOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 盘点管理 Controller（s3-3）
 * 前端调 "/api/stocktake" 接口 → 交 Service 处理。
 * 简化流程：草稿(0) → 直接过账(1)。
 * 增/查/录实盘：任何登录用户可操作；过账：仅 ADMIN/MANAGER。
 */
@RestController
@RequestMapping("/api/stocktake")
public class StockTakeController {

    @Autowired
    private StockTakeService stockTakeService;

    /** 创建盘点单（自动纳入所选仓库所有有库存商品作账面快照明细） */
    @PostMapping
    public Result<StockTakeOrder> create(@RequestBody StockTakeCreateDTO dto) {
        return Result.success(stockTakeService.create(dto));
    }

    /** 盘点单分页列表（联表仓库/经办人） */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(StockTakePageDTO dto) {
        return Result.success(stockTakeService.getPage(dto));
    }

    /** 盘点单详情（联表仓库/经办人） */
    @GetMapping("/{id}")
    public Result<StockTakeOrderVO> getOrder(@PathVariable Long id) {
        return Result.success(stockTakeService.getOrder(id));
    }

    /** 盘点明细列表（商品名/账面/实盘/盈亏） */
    @GetMapping("/{id}/items")
    public Result<List<StockTakeItemVO>> getItems(@PathVariable Long id) {
        return Result.success(stockTakeService.getItems(id));
    }

    /** 录入/修改实盘数量（仅草稿） */
    @PostMapping("/{id}/update")
    public Result<?> updateItems(@PathVariable Long id, @RequestBody List<StockTakeItemUpdateDTO> items) {
        stockTakeService.updateItems(id, items);
        return Result.success();
    }

    /** 过账：按 实盘≠账面 生成盈亏流水并调整库存（MANAGER/ADMIN） */
    @PostMapping("/{id}/post")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stocktake", action = "POST")
    public Result<StockTakeOrder> post(@PathVariable Long id) {
        return Result.success(stockTakeService.post(id));
    }
}