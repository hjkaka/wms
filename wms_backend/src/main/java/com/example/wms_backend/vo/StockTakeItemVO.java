package com.example.wms_backend.vo;

import lombok.Data;

/**
 * 盘点明细返回 VO（s3-3）
 * 联表补商品名/编码，供前端展示账面/实盘/盈亏。
 */
@Data
public class StockTakeItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private Integer systemQuantity;
    private Integer countedQuantity;
    private Integer varianceQuantity;
    private String remark;
}