package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.StockTakeItem;
import com.example.wms_backend.vo.StockTakeItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockTakeItemMapper {

    // 所选仓库的库存快照（生成盘点明细用）：productId + systemQuantity（quantity>0）
    List<StockTakeItem> getStockSnapshot(@Param("warehouseId") Long warehouseId);

    // 批量插入盘点明细
    int batchInsert(@Param("items") List<StockTakeItem> items);

    // 按盘点单查明细（联表商品名/编码）
    List<StockTakeItemVO> findByOrderId(@Param("orderId") Long orderId);

    // 按盘点单查明细实体（过账时用 productId/counted 计算盈亏）
    List<StockTakeItem> findEntitiesByOrderId(@Param("orderId") Long orderId);

    // 录入实盘数量（counted 为 null=未盘点，variance = counted - system）
    int updateCounted(@Param("id") Long id,
                      @Param("countedQuantity") Integer countedQuantity,
                      @Param("remark") String remark);
}