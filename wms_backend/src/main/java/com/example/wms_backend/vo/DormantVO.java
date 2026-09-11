package com.example.wms_backend.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 呆滞分析报表（s3-5）
 * 有库存但超过 days 天未出库的商品，按呆滞天数降序
 */
@Data
public class DormantVO {
    private Long productId;
    private String productName;
    private String productCode;
    // 当前库存数量
    private Integer quantity;
    // 当前库存金额（= 数量 × 单价）
    private BigDecimal amount;
    // 最后一次出库时间（无出库则空，按创建时间算）
    private LocalDateTime lastOutDate;
    // 呆滞天数（距最近一次出库/创建的间隔天数）
    private Integer dormantDays;
}