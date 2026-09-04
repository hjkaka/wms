package com.example.wms_backend.vo;

import lombok.Data;

@Data
public class StockSummaryVO {
    // 商品ID（用来分组）
    private Long productId;

    // 商品名（联表查出来的）
    private String productName;

    // 商品编码
    private String productCode;

    // 总库存：所有仓库加起来的数量
    private Integer totalQty;

    // 在几个仓库有存货
    private Integer warehouseCount;

    // 是否低于预警线（汇总口径下）
    private Boolean warnFlag;

}
