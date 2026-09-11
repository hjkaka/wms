package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {

    // ===== 根据ID查询 =====
    Category findById(@Param("id") Long id);

    // ===== 查询全部分类（含父子，用于树形组装） =====
    List<Category> findAll();

    // ===== 删除校验：统计该分类下子分类数量 =====
    int countChildren(@Param("parentId") Long parentId);

    // ===== 统计该分类下（含其子分类）被商品引用的数量 =====
    int countProductsByCategory(@Param("categoryId") Long categoryId);

    // ===== 新增 =====
    int insert(@Param("category") Category category);

    // ===== 修改 =====
    int update(@Param("category") Category category);

    // ===== 启停 =====
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // ===== 删除 =====
    int deleteById(@Param("id") Long id);
}