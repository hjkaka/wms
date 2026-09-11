package com.example.wms_backend.controller;

// ===== 引入所需类 =====
import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.dto.StockOutPageDTO;
import com.example.wms_backend.entity.StockOutOrder;
import com.example.wms_backend.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 出库管理 Controller
 * ===== 一句话理解 =====
 * 前端调 "/api/stockout" 接口 → 这个类接请求 → 交给 Service 处理 → 返回结果
 *
 * @RestController：接收请求返回 JSON
 * @RequestMapping("/api/stockout")：公共前缀
 */
@RestController
@RequestMapping("/api/stockout")
public class StockOutController {

    // ===== 注入 Service（业务逻辑层） =====
    @Autowired
    private StockOutService stockOutService;

    /**
     * 创建出库单
     *
     * 前端调用方式：POST /api/stockout
     * 请求体（JSON）：
     * {
     *   "warehouseId": 1,
     *   "receiver": "张三",
     *   "operatorId": 1,
     *   "remark": "销售出库",
     *   "items": [
     *     {"productId": 1, "quantity": 2, "unitPrice": 6999.00}
     *   ]
     * }
     *
     * @PostMapping：新增数据用 POST
     * @RequestBody：把请求体 JSON 转成 StockOutCreateDTO
     */
    @PostMapping
    @AuditLog(module = "stockout", action = "CREATE")
    public Result<StockOutOrder> createStockOut(@RequestBody StockOutCreateDTO dto) {
        // 交给 Service：算金额、生成单号、校验库存、扣库存、记流水
        StockOutOrder order = stockOutService.createStockOut(dto);
        return Result.success(order);
    }

    /** 分页查询出库单列表（审核流） */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(StockOutPageDTO dto) {
        return Result.success(stockOutService.getPage(dto));
    }

    // ===== s2-2 审核流端点 =====
    // 约束：库存只在"过账(post)"时变动；approve/reject/post 仅 ADMIN/MANAGER 可执行

    /** 提交审核：草稿→待审 */
    @PostMapping("/{id}/submit")
    @AuditLog(module = "stockout", action = "SUBMIT")
    public Result<StockOutOrder> submit(@PathVariable Long id) {
        return Result.success(stockOutService.submit(id));
    }

    /** 撤回：待审→草稿（制单人可撤回） */
    @PostMapping("/{id}/withdraw")
    @AuditLog(module = "stockout", action = "WITHDRAW")
    public Result<StockOutOrder> withdraw(@PathVariable Long id) {
        return Result.success(stockOutService.withdraw(id));
    }

    /** 审核通过：待审→已审（MANAGER/ADMIN） */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockout", action = "APPROVE")
    public Result<StockOutOrder> approve(@PathVariable Long id) {
        return Result.success(stockOutService.approve(id));
    }

    /** 审核驳回：待审→草稿（MANAGER/ADMIN） */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockout", action = "REJECT")
    public Result<StockOutOrder> reject(@PathVariable Long id) {
        return Result.success(stockOutService.reject(id));
    }

    /** 过账：已审→已过账，真正校验并扣减库存、记流水（MANAGER/ADMIN） */
    @PostMapping("/{id}/post")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockout", action = "POST")
    public Result<StockOutOrder> post(@PathVariable Long id) {
        return Result.success(stockOutService.post(id));
    }

    /** 红冲：已过账→已红冲，回滚库存+写反向流水+生成红冲单（MANAGER/ADMIN，强制填原因）s2-3 */
    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockout", action = "REVERSE")
    public Result<StockOutOrder> reverse(@PathVariable Long id, @RequestParam String remark) {
        return Result.success(stockOutService.reverse(id, remark));
    }
}