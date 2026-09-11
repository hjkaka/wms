package com.example.wms_backend.vo;

import lombok.Data;

/**
 * 库存查询结果视图
 * 一行 = 一个商品在某仓库的一条库存，同时带上预警标记
 */
@Data
public class StockInfoVO {
    // 库存记录ID（stock 表）
    private Long id;
    // 商品名（联表 product 查得）
    private String productName;
    // 商品编码
    private String productCode;
    // 仓库名（联表 warehouse 查得）
    private String warehouseName;
    // 当前库存数量
    private Integer quantity;
    // 锁定/在途库存数量（出库单待审/已审占用）
    private Integer lockedQuantity;
    // 可售库存 = quantity - locked_quantity
    private Integer availableQuantity;
    // 预警线（来自 product.warning_qty）
    private Integer warningQty;
    // 是否预警：quantity <= warning_qty 为 true
    private Boolean warnFlag;
}