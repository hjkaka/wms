package com.example.wms_backend.mapper;

import com.example.wms_backend.dto.StockTakePageDTO;
import com.example.wms_backend.entity.StockTakeOrder;
import com.example.wms_backend.vo.StockTakeOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockTakeOrderMapper {

    int insert(@Param("order") StockTakeOrder order);

    StockTakeOrder findById(@Param("id") Long id);

    // 默认序数板单号：PD + 日期 + 3位流水 (s3-3)
    String getMaxOrderNoByDate(@Param("prefix") String prefix);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 联表仓库名/经办人姓名的盘点单详情
    StockTakeOrderVO findVOById(@Param("id") Long id);

    // 盘点单分页列表（联表仓库名/经办人），dto 装查询条件，offset 是跳过条数
    List<StockTakeOrderVO> searchPage(@Param("dto") StockTakePageDTO dto, @Param("offset") int offset);

    // 满足条件的总条数，用于分页返回 total
    long countSearch(@Param("dto") StockTakePageDTO dto);
}