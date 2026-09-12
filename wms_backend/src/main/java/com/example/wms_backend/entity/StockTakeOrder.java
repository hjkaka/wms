package com.example.wms_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点单主表实体
 * 对应 stock_take_order 表
 */
@Data
public class StockTakeOrder {
    // 盘点单ID，自增主键
    private Long id;
    // 单号：PD + yyyyMMdd + 3位流水
    private String orderNo;
    // 盘点仓库ID
    private Long warehouseId;
    // 状态：0草稿/1已过账（见 StockTakeStatus）
    private Integer status;
    // 账面快照时间（创建盘点单时）
    private LocalDateTime snapshotTime;
    // 备注
    private String remark;
    // 盘点经办人ID
    private Long operatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}