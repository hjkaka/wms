package com.example.wms_backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockInItem {

    // 明细ID，主键
    private Long id;

    // 所属入库单ID，关联 stock_in_order 表
    private Long orderId;

    // 商品ID，关联 product 表
    private Long productId;

    // 入库数量
    private Integer quantity;

    // 单价（进价）
    private BigDecimal unitPrice;

    // 单项金额 = quantity * unitPrice
    private BigDecimal amount;

    // 创建时间
    private LocalDateTime createTime;
}