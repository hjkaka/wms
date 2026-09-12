package com.example.wms_backend.constant;

/**
 * 盘点单状态常量（s3-3 盘点流程）
 * 简化流程：草稿(0) → 直接过账(1)
 * 过账时按 实盘数≠账面数 生成盈亏流水并调整库存。
 */
public class StockTakeStatus {
    // 草稿：已创建并生成账面快照明细，可录入实盘数/修改
    public static final int DRAFT = 0;
    // 已过账：盈亏已调整库存、已写流水，不可再改
    public static final int POSTED = 1;
}