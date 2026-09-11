package com.example.wms_backend.controller;

// ===== 引入所需类 =====
import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.entity.StockOutOrder;
import com.example.wms_backend.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}