package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 盘点单分页查询条件（s3-3）
 */
@Data
public class StockTakePageDTO {
    // 状态筛选：0草稿/1已过账，不传查全部
    private Integer status;
    // 关键字：模糊匹配单号或仓库名
    private String keyword;
    // 页码，默认第1页
    private Integer pageNum = 1;
    // 每页条数，默认10条
    private Integer pageSize = 10;
}