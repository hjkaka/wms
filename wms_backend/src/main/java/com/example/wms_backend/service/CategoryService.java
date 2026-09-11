package com.example.wms_backend.service;

import com.example.wms_backend.entity.Category;
import com.example.wms_backend.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类管理（s2-4）
 */
public interface CategoryService {

    // 树形查询（含父子），用于管理页/商品分类下拉
    List<CategoryVO> tree();

    // 新增分类
    Category create(Category category);

    // 修改分类
    Category update(Long id, Category category);

    // 启停分类
    void changeStatus(Long id, Integer status);

    // 删除分类（引用中禁删）
    void delete(Long id);

    // 返回分类自身 + 所有子分类的 id 集合（用于商品按分类过滤：选父分类时包含其子分类下的商品）
    List<Long> collectRelatedIds(Long categoryId);
}