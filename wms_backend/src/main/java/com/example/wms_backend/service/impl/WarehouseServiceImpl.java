// ===== 第1行：包声明 =====
package com.example.wms_backend.service.impl;

// ===== 第2-9行：导入类 =====
import com.example.wms_backend.dto.WarehouseQueryDTO;
import com.example.wms_backend.entity.Warehouse;
import com.example.wms_backend.mapper.WarehouseMapper;
import com.example.wms_backend.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

// ===== 第10行：@Service 注解 =====
// 告诉 Spring 这是一个 Service 类
@Service
// ===== 第11行：类声明 =====
// implements WarehouseService 实现接口
public class WarehouseServiceImpl implements WarehouseService {

    // ===== 第12-13行：注入 Mapper =====
    @Autowired
    private WarehouseMapper warehouseMapper;

    // ===== 第14-22行：新增仓库 =====
    @Override
    public Warehouse createWarehouse(Warehouse warehouse) {
        // 设置默认状态为启用
        if (warehouse.getStatus() == null) {
            warehouse.setStatus(1);
        }

        // 检查仓库编码是否重复
        Warehouse existing = warehouseMapper.findByCode(warehouse.getCode());
        if (existing != null) {
            throw new RuntimeException("仓库编码已存在：" + warehouse.getCode());
        }

        // 插入数据库
        warehouseMapper.insert(warehouse);

        // 返回新增的仓库（包含自增ID）
        return warehouse;
    }

    // ===== 第24-35行：修改仓库 =====
    @Override
    public Warehouse updateWarehouse(Long id, Warehouse warehouse) {
        // 检查仓库是否存在
        Warehouse existing = warehouseMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("仓库不存在，ID：" + id);
        }

        // 检查新编码是否和其他仓库冲突
        if (warehouse.getCode() != null && !warehouse.getCode().equals(existing.getCode())) {
            Warehouse codeCheck = warehouseMapper.findByCode(warehouse.getCode());
            if (codeCheck != null) {
                throw new RuntimeException("仓库编码已存在：" + warehouse.getCode());
            }
        }

        // 设置ID
        warehouse.setId(id);

        // 更新数据库
        warehouseMapper.update(warehouse);

        // 返回更新后的仓库
        return warehouseMapper.findById(id);
    }

    // ===== 第37-43行：删除仓库 =====
    @Override
    public void deleteWarehouse(Long id) {
        // 检查仓库是否存在
        Warehouse existing = warehouseMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("仓库不存在，ID：" + id);
        }

        // 删除
        warehouseMapper.deleteById(id);
    }

    // ===== 第45-49行：根据ID查询仓库 =====
    @Override
    public Warehouse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseMapper.findById(id);
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在，ID：" + id);
        }
        return warehouse;
    }

    // ===== 第51-53行：查询所有仓库 =====
    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseMapper.findAll();
    }

    // ===== 第55-65行：条件分页查询仓库 =====
    @Override
    public Object[] queryWarehouses(WarehouseQueryDTO queryDTO) {
        // 查询列表
        List<Warehouse> list = warehouseMapper.findByCondition(
                queryDTO.getName(),
                queryDTO.getStatus(),
                queryDTO.getOffset(),
                queryDTO.getPageSize()
        );

        // 查询总数
        long total = warehouseMapper.countByCondition(
                queryDTO.getName(),
                queryDTO.getStatus()
        );

        // 返回 [列表, 总数]
        return new Object[]{list, total};
    }
}