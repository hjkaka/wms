package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 库存分页查询条件
 * 前端 GET 请求的参数，Spring 自动绑定同名属性
 */
@Data
public class StockQueryDTO {
    // 关键字：模糊匹配商品名或编码，不传查全部
    private String keyword;
    // 按仓库过滤，不传查所有仓库
    private Long warehouseId;
    // 页码，默认第1页
    private Integer pageNum = 1;
    // 每页条数，默认10条
    private Integer pageSize = 10;
}