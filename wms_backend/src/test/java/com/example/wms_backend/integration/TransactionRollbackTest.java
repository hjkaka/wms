package com.example.wms_backend.integration;

import com.example.wms_backend.constant.OrderStatus;
import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.entity.StockInOrder;
import com.example.wms_backend.entity.Stock;
import com.example.wms_backend.mapper.StockOutOrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事务回滚集成测试：
 * s3-1 起"提交审核"即锁可售库存（草稿不占锁）。本测试验证多明细提交时，
 * 若其中某明细可售库存不足，@Transactional 会把先前已成功的锁定一并回滚，
 * 单据保持草稿，不产生任何库存变更与流水——保证"要么全部锁成功，要么都不锁"。
 */
class TransactionRollbackTest extends AbstractIntegrationTest {

    @Autowired
    StockOutOrderMapper stockOutOrderMapper;

    /** 走 service 造库存（内部登录 admin）：入库→提交→审核→过账 */
    private void initStock(Long w, Long p, int qty) {
        loginAs(1L, "ADMIN");
        StockInCreateDTO in = new StockInCreateDTO();
        in.setWarehouseId(w);
        in.setSupplier("铺货");
        in.setOperatorId(1L);
        StockInCreateDTO.StockInItemDTO it = new StockInCreateDTO.StockInItemDTO();
        it.setProductId(p);
        it.setQuantity(qty);
        it.setUnitPrice(BigDecimal.valueOf(100));
        in.setItems(List.of(it));
        StockInOrder order = stockInService.createStockIn(in);
        stockInService.submit(order.getId());
        stockInService.approve(order.getId());
        stockInService.post(order.getId());
        trackStockIn(order.getId());
    }

    @Test
    void 出库提交锁定某明细可售不足整单回滚锁定() {
        Long w = createUniqueWarehouse().getId();
        Long p1 = createUniqueProduct(createUniqueCategory().getId()).getId();
        Long p2 = createUniqueProduct(createUniqueCategory().getId()).getId();
        initStock(w, p1, 3);  // p1 库存 3（可售 3）
        initStock(w, p2, 1);  // p2 库存 1（可售 1）

        loginAs(1L, "ADMIN");
        // 出库单两条明细：p1 出 1（可锁定），p2 出 5（可售不足 → 整个提交失败）
        StockOutCreateDTO out = new StockOutCreateDTO();
        out.setWarehouseId(w);
        out.setReceiver("锁定回滚测试");
        out.setOperatorId(1L);
        StockOutCreateDTO.StockOutItemDTO i1 = new StockOutCreateDTO.StockOutItemDTO();
        i1.setProductId(p1); i1.setQuantity(1); i1.setUnitPrice(BigDecimal.valueOf(100));
        StockOutCreateDTO.StockOutItemDTO i2 = new StockOutCreateDTO.StockOutItemDTO();
        i2.setProductId(p2); i2.setQuantity(5); i2.setUnitPrice(BigDecimal.valueOf(100));
        out.setItems(List.of(i1, i2));

        var order = stockOutService.createStockOut(out);

        // 提交前快照
        Stock before1 = stockMapper.findByProductAndWarehouse(p1, w);
        Stock before2 = stockMapper.findByProductAndWarehouse(p2, w);

        // 提交：p1 锁定成功后，p2 可售不足 → 抛异常
        assertThrows(RuntimeException.class, () -> stockOutService.submit(order.getId()),
                "p2 可售库存不足应使提交锁定失败");

        // 断言：p1 已加的锁定被回滚（在库与锁定恢复提交前），p2 无任何锁定
        Stock after1 = stockMapper.findByProductAndWarehouse(p1, w);
        Stock after2 = stockMapper.findByProductAndWarehouse(p2, w);
        assertNotNull(after1);
        assertNotNull(after2);
        assertEquals(before1.getQuantity(), after1.getQuantity(),
                "p1 在库数量不变（提交不扣在库）");
        assertEquals(before1.getLockedQuantity(), after1.getLockedQuantity(),
                "p1 的锁定应随事务回滚(locked_quantity 恢复提交前)");
        assertEquals(0, after2.getLockedQuantity(),
                "p2 锁定失败，不应残留锁定");
        assertEquals(before2.getQuantity(), after2.getQuantity(),
                "p2 在库不变(本来就锁不动)");

        // 断言：单据仍为草稿(0)，未进待审；也无任何 OUT 流水
        assertEquals(OrderStatus.DRAFT, stockOutOrderMapper.findById(order.getId()).getStatus(),
                "提交失败单据不应到待审，应保持草稿");
        Integer logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM stock_log WHERE product_id=? AND warehouse_id=? AND change_type='OUT'",
                Integer.class, p1, w);
        assertEquals(0, logs, "提交失败不应写入 OUT 流水");
    }
}