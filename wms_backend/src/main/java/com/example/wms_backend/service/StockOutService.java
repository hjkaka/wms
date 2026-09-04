package com.example.wms_backend.service;

import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.entity.StockOutOrder;

/**
 * 出库管理 Service 接口
 * 只定义"要做什么"，不写具体怎么实现
 * 具体实现写在 ServiceImpl 里
 */
public interface StockOutService {

    /**
     * 创建出库单
     * @param dto 前端传来的出库参数（仓库、收货人、明细列表）
     * @return 创建好的出库单（含自增ID、单号、总金额）
     */
    StockOutOrder createStockOut(StockOutCreateDTO dto);
}