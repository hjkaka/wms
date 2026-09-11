package com.example.wms_backend.constant;

/**
 * 出入库单单据状态常量（s2-2 审核流）
 * 状态机：草稿(0) → 待审(1) → 已审(2) → 已过账(3)
 * 驳回 = 从待审退回草稿(0)；撤回 = 从待审退回草稿(0)
 * 关键约束：库存只在"已过账"状态时才会变动。
 */
public class OrderStatus {

    // 草稿：已创建、未提交，明细可改
    public static final int DRAFT = 0;

    // 待审：已提交，等待 MANAGER/ADMIN 审核
    public static final int PENDING = 1;

    // 已审：审核通过，可过账（未动库存）
    public static final int APPROVED = 2;

    // 已过账：已正式生效，库存与流水已更新，不可再改
    public static final int POSTED = 3;

    // 已红冲（s2-3）：已过账(3)单据被红冲后的终态，不可再操作
    // 红冲时回滚库存、写反向流水(IN_REVERSE/OUT_REVERSE)、生成独立红冲单
    public static final int REVERSED = 4;
}