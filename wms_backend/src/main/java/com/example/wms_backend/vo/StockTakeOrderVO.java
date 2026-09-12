package com.example.wms_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点单返回 VO（s3-3）
 * 联表补仓库名、经办人姓名。
 */
@Data
public class StockTakeOrderVO {
    private Long id;
    private String orderNo;
    private Long warehouseId;
    private String warehouseName;
    private Integer status; // 0草稿/1已过账
    private LocalDateTime snapshotTime;
    private String remark;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}