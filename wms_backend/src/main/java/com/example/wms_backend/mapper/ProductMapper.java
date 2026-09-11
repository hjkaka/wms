package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {

    // ===== 根据ID查询商品 =====
    Product findById(@Param("id") Long id);

    // ===== 根据编码查询商品 =====
    Product findByCode(@Param("code") String code);

    // ===== 查询所有商品（上架的） =====
    // 用于前端列表展示
    List<Product> findAll();

    // ===== 条件分页查询商品（单分类） =====
    // 支持按名称模糊查询、按分类筛选、按状态筛选
    List<Product> findByCondition(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    // ===== 条件分页查询商品（按分类 id 集合，支持选父分类时含子分类商品） =====
    List<Product> findByConditionIds(
            @Param("name") String name,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    // ===== 统计符合条件的商品数量（单分类） =====
    long countByCondition(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status
    );

    // ===== 统计符合条件的商品数量（分类 id 集合） =====
    long countByConditionIds(
            @Param("name") String name,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("status") Integer status
    );

    // ===== 新增商品 =====
    int insert(@Param("product") Product product);

    // ===== 修改商品 =====
    int update(@Param("product") Product product);

    // ===== 删除商品 =====
    int deleteById(@Param("id") Long id);
}