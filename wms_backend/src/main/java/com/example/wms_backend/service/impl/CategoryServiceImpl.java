package com.example.wms_backend.service.impl;

import com.example.wms_backend.entity.Category;
import com.example.wms_backend.mapper.CategoryMapper;
import com.example.wms_backend.service.CategoryService;
import com.example.wms_backend.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类实现（s2-4 两级树形）
 * 删除/禁用校验：分类下仍有子分类、或被商品引用时，禁止删除。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.findAll();
        // 顶层分类（parentId==0，或父分类不存在的当作顶层，兜底）
        List<CategoryVO> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0L
                        || all.stream().noneMatch(p -> p.getId().equals(c.getParentId())))
                .map(this::toVO)
                .collect(Collectors.toList());

        // 为每个顶层分类挂上子分类
        for (CategoryVO root : roots) {
            attachChildren(root, all);
        }
        return roots;
    }

    /** 递归给节点挂子分类（两级即可，方法支持多级） */
    private void attachChildren(CategoryVO parent, List<Category> all) {
        List<Category> childList = all.stream()
                .filter(c -> parent.getId().equals(c.getParentId()))
                .sorted((a, b) -> {
                    int sa = a.getSort() == null ? 0 : a.getSort();
                    int sb = b.getSort() == null ? 0 : b.getSort();
                    return Integer.compare(sa, sb);
                })
                .collect(Collectors.toList());
        for (Category child : childList) {
            CategoryVO childVO = toVO(child);
            parent.getChildren().add(childVO);
            attachChildren(childVO, all); // 支持更深层级（为后续扩展留余地）
        }
    }

    @Override
    public Category create(Category category) {
        // 校验名称
        if (category.getName() == null || category.getName().isBlank()) {
            throw new RuntimeException("分类名称不能为空");
        }
        // 默认值：parentId=0（顶级）、sort=0、status=1（启用）
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (!category.getParentId().equals(0L)) {
            Category parent = categoryMapper.findById(category.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在，ID：" + category.getParentId());
            }
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Long id, Category category) {
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在，ID：" + id);
        }
        if (category.getName() != null && category.getName().isBlank()) {
            throw new RuntimeException("分类名称不能为空");
        }
        // 防止把自己设成自己的父分类（循环）
        if (category.getParentId() != null && category.getParentId().equals(id)) {
            throw new RuntimeException("父分类不能是自身");
        }
        category.setId(id);
        categoryMapper.update(category);
        return categoryMapper.findById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("状态参数不合法，只能是 0(禁用)/1(启用)");
        }
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在，ID：" + id);
        }
        categoryMapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long id) {
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在，ID：" + id);
        }
        // 校验1：分类下有子分类，禁止删除
        if (categoryMapper.countChildren(id) > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        // 校验2：分类被商品引用，禁止删除
        if (categoryMapper.countProductsByCategory(id) > 0) {
            throw new RuntimeException("该分类下存在商品，无法删除，请先移除或改分类");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Long> collectRelatedIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        if (categoryId == null) {
            return ids;
        }
        collect(categoryId, ids);
        return ids;
    }

    /** 收集自身 + 所有后代分类 id */
    private void collect(Long id, List<Long> acc) {
        if (acc.contains(id)) {
            return; // 防循环
        }
        acc.add(id);
        for (Category c : categoryMapper.findAll()) {
            if (id.equals(c.getParentId())) {
                collect(c.getId(), acc);
            }
        }
    }

    // ---------- 工具 ----------
    private CategoryVO toVO(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setParentId(c.getParentId());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }
}