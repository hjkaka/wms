package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 库存流水查询条件
 * 按商品/仓库/类型/时间范围筛选，分页
 */
@Data
public class StockLogQueryDTO {
    // 商品ID（精确过滤，不传查全部）
    private Long productId;
    // 仓库ID（精确过滤）
    private Long warehouseId;
    // 变动类型 IN/OUT（不传查全部）
    private String changeType;
    // 开始日期 yyyy-MM-dd（含当天）
    private String startDate;
    // 结束日期 yyyy-MM-dd（含当天）
    private String endDate;
    // 页码
    private Integer pageNum = 1;
    // 每页条数
    private Integer pageSize = 10;
}