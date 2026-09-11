package com.example.wms_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点 VO（s2-4）
 * 用于分类管理页与商品页的树形展示/下拉，包含自身字段与 children 子分类列表。
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;

    // 子分类列表（两级树形，顶级分类才有）
    private List<CategoryVO> children = new ArrayList<>();
}