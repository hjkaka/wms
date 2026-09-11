package com.example.wms_backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单实体类
 * 对应数据库 stock_out_order 表
 * 记录一次出库操作的主单信息（出到什么去向、总金额等）
 */
@Data
public class StockOutOrder {

    // 出库单ID，主键
    private Long id;

    // 出库单编号，如 CK20260821001
    // 自动生成规则：CK + 日期 + 流水号
    private String orderNo;

    // 仓库ID，关联 warehouse 表
    private Long warehouseId;

    // 收货人/领用人（出库单特有字段，入库单是 supplier 供应商）
    private String receiver;

    // 经办人ID（仓库员工），关联 sys_user 表
    private Long operatorId;

    // 出库总金额（由明细的金额合计算出）
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