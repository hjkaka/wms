package com.example.wms_backend.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * ABC 分类报表（s3-5）
 * 按期间出库金额排序，累计占比分类：
 *   A = 累计占比 ≤ 70%；B = 累计占比 ≤ 90%；C = 其余
 */
@Data
public class AbcVO {
    private Long productId;
    private String productName;
    private String productCode;
    // 期间出库数量
    private Integer outQuantity;
    // 期间出库金额
    private BigDecimal outAmount;
    // 金额占比（%）
    private Double amountRatio;
    // 累计金额占比（%）
    private Double cumulativeRatio;
    // 分类 A/B/C
    private String abcClass;
}