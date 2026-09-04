package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.entity.*;
import com.example.wms_backend.mapper.*;
import com.example.wms_backend.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 出库管理 Service 实现
 *
 * @Service：标记为 Service 类
 * @Transactional：整个方法在事务里执行
 *   出库涉及：插出库单、插明细、扣库存、记流水
 *   任何一个步骤失败 → 回滚，之前的操作全部撤销
 *
 * ===== 出库 vs 入库 的核心区别 =====
 * 1. 单号前缀：CK（出库）/ RK（入库）
 * 2. 字段公开：receiver（领用人）/ supplier（供应商）
 * 3. 【重点】扣库存方向：入库加 quantity，出库减 quantity
 * 4. 【重点】库存校验：出库必须先查库存，不够就抛异常回滚
 * 5. 流水类型：OUT / IN
 */
@Service
@Transactional
public class StockOutServiceImpl implements StockOutService {

    // ===== 注入 4 个 Mapper =====
    @Autowired
    private StockOutOrderMapper stockOutOrderMapper;

    @Autowired
    private StockOutItemMapper stockOutItemMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    public StockOutOrder createStockOut(StockOutCreateDTO dto) {

        // ===== 第1步：校验参数 =====
        // 出库明细不能为空
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("出库明细不能为空");
        }

        // ===== 第2步：计算总金额 =====
        // 遍历每条明细，amount = quantity * unitPrice，累加起来
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockOutCreateDTO.StockOutItemDTO itemDTO : dto.getItems()) {
            BigDecimal amount = itemDTO.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(amount);
        }

        // ===== 第3步：生成出库单号 =====
        // 格式：CK + 日期 + 3位流水号，如 CK20260821001
        String orderNo = generateOrderNo();

        // ===== 第4步：创建出库单主表对象 =====
        StockOutOrder order = new StockOutOrder();
        order.setOrderNo(orderNo);
        order.setWarehouseId(dto.getWarehouseId());
        order.setReceiver(dto.getReceiver());
        order.setOperatorId(dto.getOperatorId());
        order.setTotalAmount(totalAmount);
        order.setStatus(1); // 1 = 已完成
        order.setRemark(dto.getRemark());

        // ===== 第5步：插入主表 =====
        // 插入后 order.getId() 会被自动填上自增ID
        stockOutOrderMapper.insert(order);

        // ===== 第6步：插入明细表 =====
        // 把 DTO 转成 Entity，批量插入
        List<StockOutItem> items = new ArrayList<>();
        for (StockOutCreateDTO.StockOutItemDTO itemDTO : dto.getItems()) {
            StockOutItem item = new StockOutItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(itemDTO.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            items.add(item);
        }
        stockOutItemMapper.batchInsert(order.getId(), items);

        // ===== 第7步：扣减库存 + 记录流水 =====
        for (StockOutCreateDTO.StockOutItemDTO itemDTO : dto.getItems()) {
            Long productId = itemDTO.getProductId();
            Integer quantity = itemDTO.getQuantity();

            // ① 先查当前库存
            // 出库前必须确认库存是否存在、是否充足
            Stock stock = stockMapper.findByProductAndWarehouse(productId, dto.getWarehouseId());

            // ② 库存校验
            // stock == null：这个仓库根本没有该商品，无法出库
            // stock.quantity < 出库数量：库存不足，不能超卖
            int beforeQty = 0;
            if (stock == null) {
                throw new RuntimeException("库存不存在，无法出库");
            }
            beforeQty = stock.getQuantity();
            if (beforeQty < quantity) {
                throw new RuntimeException(
                        "商品ID " + productId + " 库存不足，当前库存 " + beforeQty + "，出库 " + quantity);
            }

            // ③ 扣减库存
            // decreaseQuantity 的 SQL 里带 quantity >= 出库数 条件
            // 即再次防止并发时扣成负数：
            //   返回 1 = 扣减成功；返回 0 = 条件不满足（库存不足）
            int affected = stockMapper.decreaseQuantity(productId, dto.getWarehouseId(), quantity);
            if (affected == 0) {
                throw new RuntimeException("商品ID " + productId + " 库存不足，扣减失败");
            }

            // ④ 记录出库流水
            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(dto.getWarehouseId());
            log.setChangeType("OUT"); // 出库
            log.setChangeQuantity(quantity);
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty - quantity);
            log.setOrderNo(orderNo);
            log.setOperatorId(dto.getOperatorId());
            stockLogMapper.insert(log);
        }

        // 返回创建成功的出库单
        return order;
    }

    /**
     * 生成出库单号
     * 格式：CK + yyyyMMdd + 3位流水号
     * 例如：CK20260821001
     */
    private String generateOrderNo() {
        // 今天的日期，格式 yyyyMMdd
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 前缀：CK + 日期
        String prefix = "CK" + today;

        // 查询今天最大出库单号
        String maxOrderNo = stockOutOrderMapper.getMaxOrderNoByDate(prefix);

        // 生成流水号
        int seq = 1;
        if (maxOrderNo != null && !maxOrderNo.isEmpty()) {
            // 取后3位流水号 + 1
            String seqStr = maxOrderNo.substring(maxOrderNo.length() - 3);
            seq = Integer.parseInt(seqStr) + 1;
        }

        // 格式化为3位，不足补0
        String seqFormatted = String.format("%03d", seq);
        return prefix + seqFormatted;
    }
}