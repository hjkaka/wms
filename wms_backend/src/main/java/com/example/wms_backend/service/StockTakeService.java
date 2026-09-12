package com.example.wms_backend.service;

import com.example.wms_backend.dto.StockTakeCreateDTO;
import com.example.wms_backend.dto.StockTakeItemUpdateDTO;
import com.example.wms_backend.dto.StockTakePageDTO;
import com.example.wms_backend.entity.StockTakeOrder;
import com.example.wms_backend.vo.StockTakeItemVO;
import com.example.wms_backend.vo.StockTakeOrderVO;

import java.util.List;
import java.util.Map;

/**
 * 盘点业务接口（s3-3）
 * 简化流程：草稿 → 直接过账
 * 创建时自动纳入所选仓库所有有库存商品作为账面快照明细；
 * 过账时按 实盘≠账面 生成盈亏流水并调整库存。
 */
public interface StockTakeService {
    // 创建盘点单（生成账面快照明细，status=草稿）
    StockTakeOrder create(StockTakeCreateDTO dto);

    // 盘点单详情（联表仓库/经办人）
    StockTakeOrderVO getOrder(Long id);

    // 盘点明细列表
    List<StockTakeItemVO> getItems(Long id);

    // 录入/修改实盘数量（仅草稿）
    void updateItems(Long id, List<StockTakeItemUpdateDTO> items);

    // 过账：按盈亏调整库存+写流水（仅草稿，MANAGER/ADMIN）
    StockTakeOrder post(Long id);

    // 盘点单分页列表，返回 {list, total, pageNum, pageSize}
    Map<String, Object> getPage(StockTakePageDTO dto);
}