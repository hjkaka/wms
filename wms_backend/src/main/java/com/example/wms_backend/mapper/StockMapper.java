package com.example.wms_backend.mapper;

import com.example.wms_backend.dto.StockQueryDTO;
import com.example.wms_backend.entity.Stock;
import com.example.wms_backend.vo.StockInfoVO;
import com.example.wms_backend.vo.StockSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockMapper {

    // 根据商品ID和仓库ID查询库存
    Stock findByProductAndWarehouse(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    // 新增库存记录
    int insert(@Param("stock") Stock stock);

    // 更新库存数量（增加）
    int increaseQuantity(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );

    // 更新库存数量（减少，出库用）
    // 带条件 quantity >= 出库数，防止扣成负数
    // 返回受影响行数：1=扣减成功，0=库存不足
    int decreaseQuantity(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );

    // ===== s3-1 锁定/在途库存 =====

    // 锁定可售库存（出库单"提交审核"时调用）
    // 原子操作：只在"可售库存(quantity - locked_quantity) >= 锁定数"时成功
    // 返回受影响行数：1=锁定成功，0=可售不足（被其他出库单占用）
    int lockQuantity(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );

    // 释放锁定（出库单"撤回/驳回"回草稿时调用）
    int unlockQuantity(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );

    // 过账消耗（出库单"过账"时调用）：锁定转为实际扣减，quantity 和 locked_quantity 同时减
    // 带条件 quantity >= 出库数 且 locked_quantity >= 出库数，防负数
    // 返回受影响行数：1=成功，0=库存/锁定不足
    int postDecrease(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity
    );

    // 按商品维度汇总：每个商品一行，总库存 = 各仓库求和
    // GROUP BY p.id → 一个商品只出一行
    List<StockSummaryVO> summaryByProduct();

    // 可选：按仓库维度汇总：每个仓库一行
    List<StockSummaryVO> summaryByWarehouse();

    // 已有方法保持不变，下面新增两个：

    // 分页查询库存（联表商品+仓库），dto 装查询条件，offset 是跳过条数
    List<StockInfoVO> searchPage(@Param("dto") StockQueryDTO dto, @Param("offset") int offset);

    // 满足条件的总条数，用于分页返回 total
    long countSearch(@Param("dto") StockQueryDTO dto);

}