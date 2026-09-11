package com.example.wms_backend.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单列表视图（s2-2 审核流前端列表用）
 * 在单据主字段基础上，联表补仓库名、经办人姓名
 */
@Data
public class StockInOrderVO {
    private Long id;
    private String orderNo;
    private Long warehouseId;
    private String warehouseName;   // 联表 warehouse
    private String supplier;
    private Long operatorId;
    private String operatorName;    // 联表 sys_user
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private LocalDateTime auditTime;
    private Long auditorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}