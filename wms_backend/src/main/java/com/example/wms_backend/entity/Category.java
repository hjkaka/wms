package com.example.wms_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品分类（s2-4）
 * 对应 category 表，支持两级树形：parent_id=0 为顶级分类，非 0 为其子分类。
 * 商品（product.category_id）引用叶子分类。
 */
@Data
public class Category {

    // 分类ID，主键
    private Long id;

    // 分类名称
    private String name;

    // 父分类ID，顶级填 0
    private Long parentId;

    // 排序号，数字越小越靠前
    private Integer sort;

    // 状态：0=禁用 1=启用
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;
}