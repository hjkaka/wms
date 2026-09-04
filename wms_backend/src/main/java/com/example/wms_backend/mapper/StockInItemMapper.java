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
}