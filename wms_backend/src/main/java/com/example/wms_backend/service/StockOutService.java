package com.example.wms_backend.service;

import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.dto.StockOutPageDTO;
import com.example.wms_backend.entity.StockOutOrder;

import java.util.Map;

/**
 * 出库管理 Service 接口
 * 只定义"要做什么"，不写具体怎么实现
 * 具体实现写在 ServiceImpl 里
 */
public interface StockOutService {

    /**
     * 创建出库单（草稿，状态=0，不动库存）
     * @param dto 前端传来的出库参数（仓库、收货人、明细列表）
     * @return 创建好的出库单（含自增ID、单号、总金额）
     */
    StockOutOrder createStockOut(StockOutCreateDTO dto);

    /**
     * 提交审核：草稿(0) → 待审(1)
     */
    StockOutOrder submit(Long id);

    /**
     * 撤回：待审(1) → 草稿(0)，制单人可撤回
     */
    StockOutOrder withdraw(Long id);

    /**
     * 审核通过：待审(1) → 已审(2)，记录审核人/时间（MANAGER/ADMIN）
     */
    StockOutOrder approve(Long id);

    /**
     * 审核驳回：待审(1) → 草稿(0)，记录审核人/时间（MANAGER/ADMIN）
     */
    StockOutOrder reject(Long id);

    /**
     * 过账：已审(2) → 已过账(3)，此阶段才实际校验并扣减库存、记流水（MANAGER/ADMIN）
     */
    StockOutOrder post(Long id);

    /**
     * 红冲（s2-3）：已过账(3) → 已红冲(4)，整单全额冲销。
     * 出库红冲=回滚加回库存；写 OUT_REVERSE 反向流水；生成独立红冲单(原单号+"R")。
     * 仅 ADMIN/MANAGER，必须填红冲原因。
     */
    StockOutOrder reverse(Long id, String remark);

    /**
     * 分页查询出库单（审核流列表用）
     */
    Map<String, Object> getPage(StockOutPageDTO dto);
}