package com.example.wms_backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单明细实体类
 * 对应数据库 stock_out_item 表
 * 记录出库单里每一条商品的具体出库数量、单价和金额
 */
@Data
public class StockOutItem {

    // 明细ID，主键
    private Long id;

    // 所属出库单ID，关联 stock_out_order 表
    private Long orderId;

    // 商品ID，关联 product 表
    private Long productId;

    // 出库数量
    private Integer quantity;

    // 单价（出库价）
    private BigDecimal unitPrice;

    // 单项金额 = quantity * unitPrice
    private BigDecimal amount;

    // 创建时间
    private LocalDateTime createTime;
}