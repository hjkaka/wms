package com.example.wms_backend.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报表内部聚合行（s3-5）
 * 每个上架商品一行的期间出入库聚合，供周转率/ABC 计算，不直接对外暴露
 */
@Data
public class PeriodAggVO {
    private Long productId;
    private String productName;
    private String productCode;
    // 单价（计算金额用）
    private BigDecimal price;
    // 上架时间（呆滞兜底：无出库时按创建时间算呆滞天数）
    private LocalDateTime createTime;
    // 期间出库数量
    private Integer outQuantity;
    // 期间入库数量
    private Integer inQuantity;
}