package com.example.wms_backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {

    // 商品ID，主键
    private Long id;

    // 商品名称
    private String name;

    // 商品编码（SKU），唯一
    private String code;

    // 所属分类ID，关联 category 表
    private Long categoryId;

    // 计量单位（件/箱/kg）
    private String unit;

    // 规格描述（如"红色 64G"）
    private String spec;

    // 售价（用 BigDecimal，不用 Double，避免精度问题）
    private BigDecimal price;

    // 库存预警阈值（低于这个数就提醒）
    private Integer warningQty;

    // 状态：0=下架 1=上架
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}