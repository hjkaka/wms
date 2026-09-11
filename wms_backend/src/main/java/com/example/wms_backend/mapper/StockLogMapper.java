package com.example.wms_backend.mapper;

import com.example.wms_backend.dto.StockLogQueryDTO;
import com.example.wms_backend.entity.StockLog;
import com.example.wms_backend.vo.StockLogVO;
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

    // ===== s3-6 库存流水分页查询 =====
    // 联表补 商品名/编码、仓库名、操作人姓名
    List<StockLogVO> searchPage(@Param("dto") StockLogQueryDTO dto, @Param("offset") int offset);

    // 流水总条数（用于分页）
    long countSearch(@Param("dto") StockLogQueryDTO dto);
}
