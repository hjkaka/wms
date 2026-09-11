package com.example.wms_backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockInOrder {

    // 入库单ID，主键
    private Long id;

    // 入库单编号，如 RK20260816001
    // 自动生成规则：RK + 日期 + 流水号
    private String orderNo;

    // 仓库ID，关联 warehouse 表
    private Long warehouseId;

    // 供应商名称
    private String supplier;

    // 经办人ID（仓库员工），关联 sys_user 表
    private Long operatorId;

    // 入库总金额（由明细的金额合计算出）
    private BigDecimal totalAmount;

    // 状态：0=待审核 1=已完成 2=已驳回
    private Integer status;

    // 备注
    private String remark;

    // 红冲单关联的被冲原单号（普通单为 null，红冲单记录原单号）s2-3
    private String reverseOfNo;

    // 审核时间
    private LocalDateTime auditTime;

    // 审核人ID
    private Long auditorId;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}