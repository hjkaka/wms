package com.example.wms_backend.integration;

import com.example.wms_backend.entity.Category;
import com.example.wms_backend.entity.Product;
import com.example.wms_backend.entity.Warehouse;
import com.example.wms_backend.mapper.StockMapper;
import com.example.wms_backend.service.AuthService;
import com.example.wms_backend.service.CategoryService;
import com.example.wms_backend.service.ProductService;
import com.example.wms_backend.service.StockInService;
import com.example.wms_backend.service.StockOutService;
import com.example.wms_backend.service.WarehouseService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 集成测试基类：
 * - @ActiveProfiles("test") 加载 src/test/resources/application-test.properties，指向独立 wms_test 库
 * - 每个测试的仓库/商品/分类编码唯一（时间戳+序号），@AfterEach 用 JdbcTemplate 主动清理本测试数据，
 *   既保证不污染 wms_test 库（也无真实业务主公数据），又满足"测试不留垃圾"约定。
 * - 造数统一入口：createUniqueWarehouse() / createUniqueCategory() / createUniqueProduct()
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis());

    @Autowired protected StockInService stockInService;
    @Autowired protected StockOutService stockOutService;
    @Autowired protected WarehouseService warehouseService;
    @Autowired protected ProductService productService;
    @Autowired protected CategoryService categoryService;
    @Autowired protected AuthService authService;
    @Autowired protected StockMapper stockMapper;

    protected JdbcTemplate jdbc;
    @Autowired
    public void setDataSource(DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    // 记录本测试创建的仓库/商品/分类（按 id 清理）
    private final List<Long> createdWarehouses = new ArrayList<>();
    private final List<Long> createdProducts = new ArrayList<>();
    private final List<Long> createdCategories = new ArrayList<>();
    private final List<Long> createdStockInOrders = new ArrayList<>();
    private final List<Long> createdStockOutOrders = new ArrayList<>();

    @AfterEach
    void cleanupSecurityAndData() {
        SecurityContextHolder.clearContext();
        // 清单据(先明细后主表) → 清流水/库存 → 清商品/仓库/分类
        for (Long id : createdStockInOrders) {
            jdbc.update("DELETE FROM stock_in_item WHERE order_id=?", id);
            jdbc.update("DELETE FROM stock_in_order WHERE id=?", id);
        }
        for (Long id : createdStockOutOrders) {
            jdbc.update("DELETE FROM stock_out_item WHERE order_id=?", id);
            jdbc.update("DELETE FROM stock_out_order WHERE id=?", id);
        }
        for (Long pid : createdProducts) {
            jdbc.update("DELETE FROM stock_log WHERE product_id=?", pid);
            jdbc.update("DELETE FROM stock WHERE product_id=?", pid);
        }
        for (Long id : createdProducts) {
            jdbc.update("DELETE FROM product WHERE id=?", id);
        }
        for (Long id : createdWarehouses) {
            jdbc.update("DELETE FROM warehouse WHERE id=?", id);
        }
        for (Long id : createdCategories) {
            jdbc.update("DELETE FROM category WHERE id=?", id);
        }
        createdStockInOrders.clear();
        createdStockOutOrders.clear();
        createdProducts.clear();
        createdWarehouses.clear();
        createdCategories.clear();
    }

    protected String unique(String prefix) {
        return prefix + "_" + SEQ.incrementAndGet();
    }

    protected Warehouse createUniqueWarehouse() {
        Warehouse w = new Warehouse();
        w.setName(unique("w_仓"));
        w.setCode(unique("w_code"));
        w.setAddress("测试地址");
        w.setManagerId(1L);
        w.setStatus(1);
        Warehouse created = warehouseService.createWarehouse(w);
        createdWarehouses.add(created.getId());
        return created;
    }

    protected Category createUniqueCategory() {
        Category c = new Category();
        c.setName(unique("c_类"));
        c.setParentId(0L);
        c.setSort(1);
        c.setStatus(1);
        Category created = categoryService.create(c);
        createdCategories.add(created.getId());
        return created;
    }

    protected Product createUniqueProduct(Long categoryId) {
        Product p = new Product();
        p.setName(unique("p_品"));
        p.setCode(unique("p_code"));
        p.setCategoryId(categoryId);
        p.setUnit("件");
        p.setPrice(BigDecimal.valueOf(100));
        p.setStatus(1);
        Product created = productService.createProduct(p);
        createdProducts.add(created.getId());
        return created;
    }

    protected void trackStockIn(Long id) { if (id != null) createdStockInOrders.add(id); }
    protected void trackStockOut(Long id) { if (id != null) createdStockOutOrders.add(id); }

    /** 在 SecurityContext 注入当前用户（principal = Long userId），供 getCurrentUserId() 使用 */
    protected void loginAs(Long userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}