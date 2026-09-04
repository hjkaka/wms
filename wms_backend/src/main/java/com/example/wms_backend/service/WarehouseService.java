package com.example.wms_backend.service;

import com.example.wms_backend.dto.WarehouseQueryDTO;
import com.example.wms_backend.entity.Warehouse;

import java.util.List;

public interface WarehouseService {
    // 数据保存数据库
    Warehouse createWarehouse(Warehouse warehouse);
    // 根据id更新仓库
    Warehouse updateWarehouse(Long id,Warehouse warehouse);
    // 根据id删除仓库
    void deleteWarehouse(Long id);
    //根据id查询仓库
    Warehouse getWarehouseById(Long id);
    //查询所有仓库
    List<Warehouse> getAllWarehouses();
    // ===== 条件分页查询仓库 =====
    // 返回值用自定义的分页结果类（后面会创建）
    // 暂时用 Object[] 代替，后面创建分页类后替换
    Object[] queryWarehouses(WarehouseQueryDTO queryDTO);
}
