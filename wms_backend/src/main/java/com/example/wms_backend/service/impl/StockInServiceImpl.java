package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.entity.*;
import com.example.wms_backend.mapper.*;
import com.example.wms_backend.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Service：标记为 Service 类
 * @Transactional：声明这个类的方法都在事务中执行
 * 事务 = 一系列操作要么全部成功，要么全部失败
 * 比如：入库单插入成功，但库存更新失败 → 整个操作回滚（入库单也撤销）
 */
@Service
@Transactional  // 事务管理
public class StockInServiceImpl implements StockInService {

    // ===== 注入 4 个 Mapper =====
    @Autowired
    private StockInOrderMapper stockInOrderMapper;

    @Autowired
    private StockInItemMapper stockInItemMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    public StockInOrder createStockIn(StockInCreateDTO dto) {

        // ===== 第1步：校验参数 =====
        // 检查明细列表是否为空
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("入库明细不能为空");
        }

        // ===== 第2步：计算总金额 =====
        // 遍历每条明细，计算 amount = quantity * unitPrice
        // 累加总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockInCreateDTO.StockInItemDTO itemDTO : dto.getItems()) {
            // 计算单项金额
            BigDecimal amount = itemDTO.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            // 累加
            totalAmount = totalAmount.add(amount);
        }

        // ===== 第3步：生成入库单号 =====
        // 格式：RK + 日期 + 3位流水号
        // 例如：RK20260816001
        String orderNo = generateOrderNo();

        // ===== 第4步：创建入库单主表对象 =====
        StockInOrder order = new StockInOrder();
        order.setOrderNo(orderNo);
        order.setWarehouseId(dto.getWarehouseId());
        order.setSupplier(dto.getSupplier());
        order.setOperatorId(dto.getOperatorId());
        order.setTotalAmount(totalAmount);
        order.setStatus(1); // 1 = 已完成（直接生效，不需要审核）
        order.setRemark(dto.getRemark());

        // ===== 第5步：插入主表 =====
        // 插入后，order.getId() 会被自动填上自增ID
        stockInOrderMapper.insert(order);

        // ===== 第6步：插入明细表 =====
        // 把 DTO 转成 Entity
        List<StockInItem> items = new ArrayList<>();
        for (StockInCreateDTO.StockInItemDTO itemDTO : dto.getItems()) {
            StockInItem item = new StockInItem();
            item.setOrderId(order.getId()); // 关联主表ID
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            // 计算单项金额
            item.setAmount(itemDTO.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            items.add(item);
        }
        // 批量插入
        stockInItemMapper.batchInsert(order.getId(), items);

        // ===== 第7步：更新库存 + 记录流水 =====
        // 遍历每条明细，更新库存并记录流水
        for (StockInCreateDTO.StockInItemDTO itemDTO : dto.getItems()) {
            Long productId = itemDTO.getProductId();
            Integer quantity = itemDTO.getQuantity();

            // 查询当前库存
            Stock stock = stockMapper.findByProductAndWarehouse(productId, dto.getWarehouseId());

            int beforeQty = 0; // 变动前的库存数量
            if (stock == null) {
                // 库存表中没有这条记录 → 新增
                stock = new Stock();
                stock.setProductId(productId);
                stock.setWarehouseId(dto.getWarehouseId());
                stock.setQuantity(quantity);
                stockMapper.insert(stock);
                beforeQty = 0;
            } else {
                // 已有记录 → 增加数量
                beforeQty = stock.getQuantity();
                stockMapper.increaseQuantity(productId, dto.getWarehouseId(), quantity);
            }

            // 记录库存流水
            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(dto.getWarehouseId());
            log.setChangeType("IN"); // 入库
            log.setChangeQuantity(quantity);
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty + quantity);
            log.setOrderNo(orderNo);
            log.setOperatorId(dto.getOperatorId());
            stockLogMapper.insert(log);
        }

        // 返回创建成功的入库单
        return order;
    }

    /**
     * 生成入库单号
     * 格式：RK + 日期 + 3位流水号
     * 例如：RK20260816001
     *
     * 生成规则：
     * 1. 查询今天最大的入库单号
     * 2. 如果今天没有入库单，从 001 开始
     * 3. 如果有，流水号 + 1
     *
     * @return 入库单号
     */
    private String generateOrderNo() {
        // 获取今天的日期，格式：yyyyMMdd
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 前缀：RK + 日期
        String prefix = "RK" + today;

        // 查询今天最大的入库单号
        String maxOrderNo = stockInOrderMapper.getMaxOrderNoByDate(prefix);

        // 生成流水号
        int seq = 1; // 默认从 001 开始
        if (maxOrderNo != null && !maxOrderNo.isEmpty()) {
            // 从入库单号中提取后3位流水号，+1
            String seqStr = maxOrderNo.substring(maxOrderNo.length() - 3);
            seq = Integer.parseInt(seqStr) + 1;
        }

        // 格式化为3位数字，不足补0
        // 例如：1 → "001"，10 → "010"，100 → "100"
        String seqFormatted = String.format("%03d", seq);

        // 拼接完整的入库单号
        return prefix + seqFormatted;
    }
}