package com.example.wms_backend.service;

import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.entity.StockInOrder;

public interface StockInService {
    StockInOrder createStockIn(StockInCreateDTO dto);
}
