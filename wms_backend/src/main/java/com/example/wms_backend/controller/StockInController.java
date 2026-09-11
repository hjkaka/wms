package com.example.wms_backend.controller;

// ===== 1. 定义 REST 接口的位置和返回类型 =====
import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.dto.StockInPageDTO;
import com.example.wms_backend.entity.StockInOrder;
import com.example.wms_backend.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 入库管理 Controller
 * ===== 一句话理解 =====
 * 前端调 "/api/stockin" 接口 → 这个类接收请求 → 交给 Service 层处理 → 把结果返回给前端
 *
 * @RestController：这个类是"接收请求"的类
 * @RequestMapping("/api/stockin")：所有接口的公共前缀
 */
@RestController
@RequestMapping("/api/stockin")
public class StockInController {

    // ===== 注入 Service（业务逻辑层） =====
    // Controller 自己不做业务，只负责"接请求"和"返回结果"
    @Autowired
    private StockInService stockInService;

    /**
     * 创建入库单
     *
     * 前端调用方式：POST /api/stockin
     * 请求体（JSON）：
     * {
     *   "warehouseId": 1,
     *   "supplier": "供应商A",
     *   "operatorId": 1,
     *   "remark": "备注",
     *   "items": [
     *     {"productId": 1, "quantity": 10, "unitPrice": 5.5},
     *     {"productId": 2, "quantity": 5,  "unitPrice": 3.2}
     *   ]
     * }
     *
     * @PostMapping：表示这个接口用 POST 方法（新增数据用 POST）
     * @RequestBody：把请求体里的 JSON 自动转成 StockInCreateDTO 对象
     * Result<StockInOrder>：统一返回格式
     */
    @PostMapping
    @AuditLog(module = "stockin", action = "CREATE")
    public Result<StockInOrder> createStockIn(@RequestBody StockInCreateDTO dto) {
        // 交给 Service 层处理业务（计算金额、生成单号、存草稿）
        StockInOrder order = stockInService.createStockIn(dto);
        // 返回成功结果，data 里放创建好的入库单
        return Result.success(order);
    }

    /** 分页查询入库单列表（审核流） */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(StockInPageDTO dto) {
        return Result.success(stockInService.getPage(dto));
    }

    // ===== s2-2 审核流端点 =====
    // 约束：库存只在"过账(post)"时变动；approve/reject/post 仅 ADMIN/MANAGER 可执行

    /** 提交审核：草稿→待审 */
    @PostMapping("/{id}/submit")
    @AuditLog(module = "stockin", action = "SUBMIT")
    public Result<StockInOrder> submit(@PathVariable Long id) {
        return Result.success(stockInService.submit(id));
    }

    /** 撤回：待审→草稿（制单人可撤回） */
    @PostMapping("/{id}/withdraw")
    @AuditLog(module = "stockin", action = "WITHDRAW")
    public Result<StockInOrder> withdraw(@PathVariable Long id) {
        return Result.success(stockInService.withdraw(id));
    }

    /** 审核通过：待审→已审（MANAGER/ADMIN） */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockin", action = "APPROVE")
    public Result<StockInOrder> approve(@PathVariable Long id) {
        return Result.success(stockInService.approve(id));
    }

    /** 审核驳回：待审→草稿（MANAGER/ADMIN） */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockin", action = "REJECT")
    public Result<StockInOrder> reject(@PathVariable Long id) {
        return Result.success(stockInService.reject(id));
    }

    /** 过账：已审→已过账，真正更新库存并记流水（MANAGER/ADMIN） */
    @PostMapping("/{id}/post")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockin", action = "POST")
    public Result<StockInOrder> post(@PathVariable Long id) {
        return Result.success(stockInService.post(id));
    }

    /** 红冲：已过账→已红冲，回滚库存+写反向流水+生成红冲单（MANAGER/ADMIN，强制填原因）s2-3 */
    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "stockin", action = "REVERSE")
    public Result<StockInOrder> reverse(@PathVariable Long id, @RequestParam String remark) {
        return Result.success(stockInService.reverse(id, remark));
    }
}