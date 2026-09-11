package com.example.wms_backend.controller;

import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.entity.Category;
import com.example.wms_backend.service.CategoryService;
import com.example.wms_backend.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理接口（s2-4）
 * 路径：/api/category/**
 * 查询：任意已登录角色可看树；增/改/启停/删：限 ADMIN/MANAGER + @AuditLog
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ===== 树形查询全部分类 =====
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    // ===== 新增分类 =====
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "category", action = "CREATE")
    public Result<Category> create(@RequestBody Category category) {
        return Result.success(categoryService.create(category));
    }

    // ===== 修改分类 =====
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "category", action = "UPDATE")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.success(categoryService.update(id, category));
    }

    // ===== 启停分类 =====
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "category", action = "UPDATE")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.changeStatus(id, status);
        return Result.success();
    }

    // ===== 删除分类（引用中禁删） =====
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "category", action = "DELETE")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}