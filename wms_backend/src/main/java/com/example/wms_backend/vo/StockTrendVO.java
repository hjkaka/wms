package com.example.wms_backend.vo;

import lombok.Data;

/**
 * 出入库日报趋势视图
 * 一行 = 某一天（某类型）的汇总
 */
@Data
public class StockTrendVO {
    // 日期（字符串，因为 SQL 按天分组后的 result 是 DATE 类型）
    private String date;
    // 类型：IN=入库 / OUT=出库（字符串直接返回，前端展示）
    private String changeType;
    // 该日该类型的总数量
    private Integer totalQty;
}