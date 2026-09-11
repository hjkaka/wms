package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.StockOutItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 出库单明细 Mapper 接口
 * 负责操作 stock_out_item 表
 */
@Mapper
public interface StockOutItemMapper {

    // 批量插入出库明细
    // orderId = 所属出库单ID，items = 明细列表
    int batchInsert(@Param("orderId") Long orderId, @Param("items") List<StockOutItem> items);

    // 按出库单ID查询明细（过账时校验安全库存并扣减）
    List<StockOutItem> findByOrderId(@Param("orderId") Long orderId);
}