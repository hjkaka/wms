package com.example.wms_backend.controller;


import com.example.wms_backend.annotation.AuditLog;
import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.ProductQueryDTO;
import com.example.wms_backend.entity.Product;
import com.example.wms_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;


    // 新增商品
    // 只有 管理员/仓库主管 能维护主数据（STAFF 只读+出入库）
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "product", action = "CREATE")
    public Result<Product> creatProduct(@RequestBody Product product){
        Product created = productService.createProduct(product);
        return Result.success(created);
    }

    //修改商品
    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "product", action = "UPDATE")
    public Result<Product> updataProduct(@PathVariable Long id, @RequestBody Product product){
        Product updated = productService.updateProduct(id, product);
        return Result.success(updated);
    }

    //删除商品
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @AuditLog(module = "product", action = "DELETE")
    public Result<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return Result.success();
    }

    //根据id查询商品
    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id){
        Product product = productService.getProductById(id);
        return Result.success(product);
    }

    //查询所有上架商品
    @GetMapping("/list")
    public Result<List<Product>> getAllProducts() {
        // 调用 Service 层的查询所有方法
        List<Product> list = productService.getAllProducts();
        return Result.success(list);
    }

    //条件分页查询商品
    @GetMapping("/page")
    public Result<Map<String,Object>> quertyProducts(ProductQueryDTO queryDTO){
        Object[] result=productService.queryProducts(queryDTO);
        // ===== 第2步：从数组中取出数据 =====
        // 强制类型转换：(List<Product>) 将 Object 转成 List<Product>
        // 强制类型转换：如果类型不对，会抛出 ClassCastException
        // 但因为我们在 Service 里就是这么放的，所以类型肯定对
        //@SuppressWarnings("unchecked")
        List<Product> list = (List<Product>) result[0];
        // 这里的 result[1] 是 long 类型
        // 但因为 Object[] 里存的是 long，所以自动装箱成 Long
        long total = (long) result[1];

        // ===== 第3步：组装分页结果 =====
        // 用 HashMap 来装分页数据
        // HashMap 是最常用的 Map 实现，底层是哈希表
        // put(key, value)：存入键值对
        // key 是 String 类型，value 是 Object 类型（可以放任何类型）
        Map<String, Object> pageResult = new HashMap<>();
        // 数据列表
        pageResult.put("list", list);
        // 总记录数
        pageResult.put("total", total);
        // 当前页码
        pageResult.put("pageNum", queryDTO.getPageNum());
        // 每页条数
        pageResult.put("pageSize", queryDTO.getPageSize());

        // ===== 第4步：返回 =====
        return Result.success(pageResult);
    }
}
