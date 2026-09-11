package com.example.wms_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作审计日志实体
 * 对应表 audit_log，记录"谁、何时、对哪个数据、做了什么改变"
 */
@Data
public class AuditLog {
    private Long id;
    /** 所属模块：product / warehouse / stockin / stockout */
    private String module;
    /** 动作：CREATE / UPDATE / DELETE */
    private String action;
    /** 受影响数据的主键ID */
    private Long targetId;
    /** 业务单号（出入库单号/编码） */
    private String targetNo;
    /** 变更详情（before→after 快照） */
    private String detail;
    /** 操作人用户ID */
    private Long operatorId;
    /** 操作人用户名 */
    private String operatorName;
    /** 请求路径 */
    private String requestUri;
    /** 客户端IP */
    private String ip;
    private LocalDateTime createTime;
}