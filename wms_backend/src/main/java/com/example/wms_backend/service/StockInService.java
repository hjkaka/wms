package com.example.wms_backend.service;

import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.dto.StockInPageDTO;
import com.example.wms_backend.entity.StockInOrder;

import java.util.Map;

public interface StockInService {

    /**
     * 创建入库单（草稿，状态=0，不动库存）
     * 库存真正变动发生在 "过账(post)" 阶段
     */
    StockInOrder createStockIn(StockInCreateDTO dto);

    /**
     * 提交审核：草稿(0) → 待审(1)
     */
    StockInOrder submit(Long id);

    /**
     * 撤回：待审(1) → 草稿(0)，制单人可撤回
     */
    StockInOrder withdraw(Long id);

    /**
     * 审核通过：待审(1) → 已审(2)，记录审核人/时间（MANAGER/ADMIN）
     */
    StockInOrder approve(Long id);

    /**
     * 审核驳回：待审(1) → 草稿(0)，记录审核人/时间（MANAGER/ADMIN）
     */
    StockInOrder reject(Long id);

    /**
     * 过账：已审(2) → 已过账(3)，此阶段才实际更新库存并记流水（MANAGER/ADMIN）
     */
    StockInOrder post(Long id);

    /**
     * 分页查询入库单（审核流列表用）
     */
    Map<String, Object> getPage(StockInPageDTO dto);
}
