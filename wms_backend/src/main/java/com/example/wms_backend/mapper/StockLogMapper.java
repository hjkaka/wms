package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.StockLog;
import com.example.wms_backend.vo.StockTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockLogMapper {
    int insert (@Param("log") StockLog log);

    // 按天统计出入库数量趋势
    // 参数 startDate/endDate 是日期字符串 yyyy-MM-dd
    List<StockTrendVO> trendDaily(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
