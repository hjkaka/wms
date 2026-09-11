package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.StockInItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StockInItemMapper {

    // 批量插入入库明细
    // orderId = 所属入库单ID，items = 明细列表
    int batchInsert(@Param("orderId") Long orderId, @Param("items") List<StockInItem> items);

    // 按入库单ID查询明细（过账时读取每条的 productId/quantity 来更新库存）
    List<StockInItem> findByOrderId(@Param("orderId") Long orderId);
}