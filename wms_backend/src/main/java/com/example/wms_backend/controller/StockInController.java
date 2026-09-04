package com.example.wms_backend.controller;

// ===== 1. 定义 REST 接口的位置和返回类型 =====
import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.entity.StockInOrder;
import com.example.wms_backend.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<StockInOrder> createStockIn(@RequestBody StockInCreateDTO dto) {
        // 交给 Service 层处理业务（计算金额、生成单号、更新库存、记流水）
        StockInOrder order = stockInService.createStockIn(dto);
        // 返回成功结果，data 里放创建好的入库单
        return Result.success(order);
    }
}