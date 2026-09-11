package com.example.wms_backend.vo;

import lombok.Data;

/**
 * 库存周转率报表（s3-5）
 * 口径按金额：期间出库金额 = 出库数量 × 单价
 * 周转次数 = 期间出库数量 / 平均库存数量
 */
@Data
public class TurnoverVO {
    private Long productId;
    private String productName;
    private String productCode;
    // 期间出库数量
    private Integer outQuantity;
    // 期间出库金额
    private java.math.BigDecimal outAmount;
    // 期初库存数量（= 期末 - 期间净变动）
    private Integer beginQuantity;
    // 期末(当前)库存数量
    private Integer endQuantity;
    // 平均库存数量 = (期初+期末)/2
    private Double avgQuantity;
    // 库存周转次数
    private Double turnoverTimes;
    // 库存周转天数（= 期间天数/周转次数）
    private Double turnoverDays;
}