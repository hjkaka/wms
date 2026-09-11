package com.example.wms_backend.integration;

import com.example.wms_backend.constant.OrderStatus;
import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.entity.StockInOrder;
import com.example.wms_backend.entity.Stock;
import com.example.wms_backend.mapper.StockInItemMapper;
import com.example.wms_backend.mapper.StockInOrderMapper;
import com.example.wms_backend.mapper.StockOutOrderMapper;
import com.example.wms_backend.service.AuthService;
import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.dto.LoginDTO;
import com.example.wms_backend.dto.LoginVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 出库过账 + 防超卖集成测试：
 * 1) 出库"提交审核"锁定可售库存(locked_quantity)，未过账不实际扣减；
 * 2) 过账消耗锁定并扣减 on-hand；
 * 3) 防超卖：可售库存不足时提交失败且保留草稿。
 */
class StockOutIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    StockOutOrderMapper stockOutOrderMapper;
    @Autowired
    StockInOrderMapper stockInOrderMapper;
    @Autowired
    StockInItemMapper stockInItemMapper;
    @Autowired
    AuthService authServiceReal;

    private void loginAdmin() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        LoginVO vo = authService.login(dto);
        assertNotNull(vo.getToken(), "测试库 admin 登录应成功(已预置bcrypt账号)");
        loginAs(vo.getUserId(), "ADMIN");
    }

    /** 先做一笔入库过账，给商品在"仓库"铺库存 n */
    private Long initStock(Long w, Long p, int qty) {
        StockInCreateDTO in = new StockInCreateDTO();
        in.setWarehouseId(w);
        in.setSupplier("铺货");
        in.setOperatorId(1L);
        StockInCreateDTO.StockInItemDTO it = new StockInCreateDTO.StockInItemDTO();
        it.setProductId(p);
        it.setQuantity(qty);
        it.setUnitPrice(BigDecimal.valueOf(100));
        in.setItems(List.of(it));
        loginAdmin();
        StockInOrder order = stockInService.createStockIn(in);
        stockInService.submit(order.getId());
        stockInService.approve(order.getId());
        stockInService.post(order.getId());
        trackStockIn(order.getId());
        return order.getId();
    }

    private StockOutCreateDTO outDto(Long w, Long p, int qty, String receiver) {
        StockOutCreateDTO dto = new StockOutCreateDTO();
        dto.setWarehouseId(w);
        dto.setReceiver(receiver);
        dto.setOperatorId(1L);
        StockOutCreateDTO.StockOutItemDTO it = new StockOutCreateDTO.StockOutItemDTO();
        it.setProductId(p);
        it.setQuantity(qty);
        it.setUnitPrice(BigDecimal.valueOf(100));
        dto.setItems(List.of(it));
        return dto;
    }

    @Test
    void 出库提交锁定过账扣减() {
        Long w = createUniqueWarehouse().getId();
        Long p = createUniqueProduct(createUniqueCategory().getId()).getId();
        initStock(w, p, 20); // 铺货 20

        loginAdmin();
        var out = stockOutService.createStockOut(outDto(w, p, 6, "领用人A"));
        trackStockOut(out.getId());

        // 提交审核 → 锁定量=6，在库数量不变
        stockOutService.submit(out.getId());
        Stock locked = stockMapper.findByProductAndWarehouse(p, w);
        assertEquals(20, locked.getQuantity(), "提交锁定不应改在库(quantity)");
        assertEquals(6, locked.getLockedQuantity(), "提交应锁定6");

        // 审核 → 已审
        stockOutService.approve(out.getId());
        // 过账 → 消耗锁定并扣减在库
        stockOutService.post(out.getId());

        Stock after = stockMapper.findByProductAndWarehouse(p, w);
        assertEquals(14, after.getQuantity(), "过账后在库应扣到14");
        assertEquals(0, after.getLockedQuantity(), "过账后锁定应清零");

        Integer outLogs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM stock_log WHERE product_id=? AND warehouse_id=? AND change_type='OUT'",
                Integer.class, p, w);
        assertEquals(1, outLogs, "应恰好有1条 OUT 流水");
        assertEquals(OrderStatus.POSTED, stockOutOrderMapper.findById(out.getId()).getStatus());
    }

    @Test
    void 防超卖可售库存不足提交失败且回到草稿() {
        Long w = createUniqueWarehouse().getId();
        Long p = createUniqueProduct(createUniqueCategory().getId()).getId();
        initStock(w, p, 5); // 铺货 5

        loginAdmin();
        // 第一张出库单锁 5，把可售占满
        var out1 = stockOutService.createStockOut(outDto(w, p, 5, "领用人B"));
        trackStockOut(out1.getId());
        stockOutService.submit(out1.getId());

        // 第二张出库单要 2，可售库存(5-5=0)不足 → 提交应抛异常且不可继续
        var out2 = stockOutService.createStockOut(outDto(w, p, 2, "领用人C"));
        trackStockOut(out2.getId());

        assertThrows(RuntimeException.class, () -> stockOutService.submit(out2.getId()),
                "可售库存不足时提交应抛异常(事务回滚)");

        // 第二张单保持草稿，且未产生新锁定
        assertEquals(OrderStatus.DRAFT, stockOutOrderMapper.findById(out2.getId()).getStatus(),
                "提交失败后应保持草稿");
        Stock s = stockMapper.findByProductAndWarehouse(p, w);
        assertEquals(5, s.getLockedQuantity(), "锁定仍应为 out1 的5，失败单未占用额外锁定");
    }
}