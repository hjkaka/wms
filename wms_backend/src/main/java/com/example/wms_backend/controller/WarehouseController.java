package com.example.wms_backend.controller;

import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.WarehouseQueryDTO;
import com.example.wms_backend.entity.Warehouse;
import com.example.wms_backend.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    // ===== 新增仓库 =====
    // POST /api/warehouse
    @PostMapping
    public Result<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        Warehouse created = warehouseService.createWarehouse(warehouse);
        return Result.success(created);
    }

    // ===== 修改仓库 =====
    // PUT /api/warehouse/{id}
    // 注意：这里用 @PutMapping，不是 @PostMapping
    @PutMapping("/{id}")
    public Result<Warehouse> updateWarehouse(
            @PathVariable Long id,
            @RequestBody Warehouse warehouse) {
        Warehouse updated = warehouseService.updateWarehouse(id, warehouse);
        return Result.success(updated);
    }

    // ===== 删除仓库 =====
    // DELETE /api/warehouse/{id}
    @DeleteMapping("/{id}")
    public Result<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success();
    }

    // ===== 根据ID查询仓库 =====
    // GET /api/warehouse/{id}
    // 注意：带参数的路径放在最后，避免和 list/page 冲突
    @GetMapping("/{id}")
    public Result<Warehouse> getWarehouseById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        return Result.success(warehouse);
    }

    // ===== 查询所有仓库 =====
    // GET /api/warehouse/list
    @GetMapping("/list")
    public Result<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> list = warehouseService.getAllWarehouses();
        return Result.success(list);
    }

    // ===== 条件分页查询仓库 =====
    // GET /api/warehouse/page?pageNum=1&pageSize=10
    @GetMapping("/page")
    public Result<Map<String, Object>> queryWarehouses(WarehouseQueryDTO queryDTO) {
        Object[] result = warehouseService.queryWarehouses(queryDTO);
        List<Warehouse> list = (List<Warehouse>) result[0];
        long total = (long) result[1];

        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", list);
        pageResult.put("total", total);
        pageResult.put("pageNum", queryDTO.getPageNum());
        pageResult.put("pageSize", queryDTO.getPageSize());

        return Result.success(pageResult);
    }
}