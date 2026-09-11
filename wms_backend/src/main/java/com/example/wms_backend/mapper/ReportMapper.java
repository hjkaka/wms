package com.example.wms_backend.mapper;

import com.example.wms_backend.vo.PeriodAggVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报表查询（s3-5）
 * 提供报表计算所需的基础聚合数据
 */
@Mapper
public interface ReportMapper {

    // 每个上架商品一行的期间出入库聚合（LEFT JOIN 子查询，无流水商品也出现且为0）
    List<PeriodAggVO> periodAgg(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // 当前(期末)各商品库存数量：product_id -> sum(quantity)
    List<Map<String, Object>> currentStockQuantity();

    // 各商品最后一次出库时间：product_id -> max(create_time) where OUT
    List<Map<String, Object>> lastOutDate();
}