package com.example.wms_backend.service;

import com.example.wms_backend.dto.StockLogQueryDTO;
import com.example.wms_backend.dto.StockQueryDTO;
import com.example.wms_backend.vo.StockInfoVO;
import com.example.wms_backend.vo.StockSummaryVO;
import com.example.wms_backend.vo.StockTrendVO;
import java.util.List;
import java.util.Map;

/**
 * 库存查询业务接口
 */
public interface StockService {

    // 分页查询库存，返回 {list, total, pageNum, pageSize}
    Map<String, Object> queryStockPage(StockQueryDTO dto);

    // 按商品维度汇总
    List<StockSummaryVO> summaryByProduct();

    // 按仓库维度汇总
    List<StockSummaryVO> summaryByWarehouse();

    // 出入库日报趋势（按天统计出入库数量）
    List<StockTrendVO> queryTrend(String startDate, String endDate);

    // ===== s3-6 库存流水分页查询，返回 {list, total, pageNum, pageSize}
    Map<String, Object> queryLogPage(StockLogQueryDTO dto);
}