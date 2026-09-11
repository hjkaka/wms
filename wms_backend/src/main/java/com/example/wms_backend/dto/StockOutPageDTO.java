package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 出库单分页查询条件（s2-2 审核流前端列表用）
 */
@Data
public class StockOutPageDTO {
    // 状态筛选：0草稿/1待审/2已审/3已过账，不传查全部
    private Integer status;
    // 关键字：模糊匹配单号或领用人
    private String keyword;
    // 页码，默认第1页
    private Integer pageNum = 1;
    // 每页条数，默认10条
    private Integer pageSize = 10;
}