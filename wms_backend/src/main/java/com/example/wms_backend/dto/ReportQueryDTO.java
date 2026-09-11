package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 报表查询条件（s3-5）
 * 支持时间段筛选；呆滞分析的宽松判定天数 days 可配
 */
@Data
public class ReportQueryDTO {
    // 开始日期 yyyy-MM-dd（含当天）
    private String startDate;
    // 结束日期 yyyy-MM-dd（含当天）
    private String endDate;
    // 呆滞判定天数（默认30：超过N天未出库即视为呆滞）
    private Integer days = 30;
}