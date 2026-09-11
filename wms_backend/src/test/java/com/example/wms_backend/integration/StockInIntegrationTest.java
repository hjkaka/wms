package com.example.wms_backend.integration;

import com.example.wms_backend.constant.OrderStatus;
import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.entity.Stock;
import com.example.wms_backend.entity.StockInOrder;
import com.example.wms_backend.entity.StockInItem;
import com.example.wms_backend.mapper.StockInItemMapper;
import com.example.wms_backend.mapper.StockInOrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 入库过账集成测试（核心事务）：草稿→提交→审核→过账，验证库存真正变动 + IN 流水 + 状态流转。
 */
class StockInIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    StockInOrderMapper stockInOrderMapper;
    @Autowired
    StockInItemMapper stockInItemMapper;

    @Test
    void 入库过账后库存增加且写IN流水且状态为已过账() {
        Long w = createUniqueWarehouse().getId();
        Long p = createUniqueProduct(createUniqueCategory().getId()).getId();

        // 建草稿(0) → 不应动库存
        StockInCreateDTO dto = new StockInCreateDTO();
        dto.setWarehouseId(w);
        dto.setSupplier("测试供应商");
        dto.setOperatorId(1L);
        StockInCreateDTO.StockInItemDTO it = new StockInCreateDTO.StockInItemDTO();
        it.setProductId(p);
        it.setQuantity(10);
        it.setUnitPrice(BigDecimal.valueOf(50));
        dto.setItems(List.of(it));
        StockInOrder order = stockInService.createStockIn(dto);
        trackStockIn(order.getId());

        // 草稿阶段库存应为空
        Stock before = stockMapper.findByProductAndWarehouse(p, w);
        assertNull(before, "创建草稿不应动库存(无库存行)");

        // 提交→待审(1)
        loginAs(1L, "ADMIN");
        stockInService.submit(order.getId());
        // 审核→已审(2)
        stockInService.approve(order.getId());
        // 过账→已过账(3)，此时才动库存
        stockInService.post(order.getId());

        // 断言 1：库存增加
        Stock after = stockMapper.findByProductAndWarehouse(p, w);
        assertNotNull(after, "过账后应有库存行");
        assertEquals(10, after.getQuantity(), "过账后库存数量应为10");

        // 断言 2：IN 流水
        Integer inLogs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM stock_log WHERE product_id=? AND warehouse_id=? AND change_type='IN'",
                Integer.class, p, w);
        assertEquals(1, inLogs, "应恰好有1条 IN 流水");

        // 断言 3：状态已过账
        StockInOrder reloaded = stockInOrderMapper.findById(order.getId());
        assertEquals(OrderStatus.POSTED, reloaded.getStatus(), "过账后状态应为已过账(3)");

        // 断言 4：明细已写入
        List<StockInItem> items = stockInItemMapper.findByOrderId(order.getId());
        assertEquals(1, items.size(), "应写入1条明细");
        assertEquals(p, items.get(0).getProductId());
        assertEquals(10, items.get(0).getQuantity());
    }
}