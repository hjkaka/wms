package com.example.wms_backend.service.impl;

import com.example.wms_backend.constant.OrderStatus;
import com.example.wms_backend.dto.StockOutCreateDTO;
import com.example.wms_backend.dto.StockOutPageDTO;
import com.example.wms_backend.vo.StockOutOrderVO;
import com.example.wms_backend.entity.*;
import com.example.wms_backend.mapper.*;
import com.example.wms_backend.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出库管理 Service 实现
 *
 * @Service：标记为 Service 类
 * @Transactional：整个方法在事务里执行
 *   出库涉及：查出库单、校验库存、扣库存、记流水
 *   任何一个步骤失败 → 回滚，之前的操作全部撤销
 *
 * ===== s2-2 审核流重构 =====
 * 状态机：草稿(0) → 待审(1) → 已审(2) → 已过账(3)
 * 关键约束：库存只在"过账(post)"阶段才会变动
 * 权限：STAFF 创建+提交+撤回；MANAGER/ADMIN 审核(approve/reject)+过账(post)
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

    // ============ 公共方法 =============

    /**
     * 获取当前登录用户ID（从 SecurityContext 取出）
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new RuntimeException("获取当前用户失败，请检查登录状态");
    }

    /**
     * 校验状态转换合法性
     */
    private void checkTransition(Integer currentStatus, int targetStatus, int... allowed) {
        for (int a : allowed) {
            if (currentStatus == a) {
                return;
            }
        }
        throw new RuntimeException(String.format(
                "状态不合法：当前%d 无法转到%d", currentStatus, targetStatus));
    }

    // ============ 业务接口实现 =============

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
        order.setStatus(OrderStatus.DRAFT); // 新建 = 草稿（不动库存）
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

        // ===== 【s2-2 改变】这里不再立刻扣库存，只存草稿 =====
        // 库存校验 & 扣减推迟到 "过账(post)" 阶段执行

        // 返回创建成功的出库单
        return order;
    }

    @Override
    public StockOutOrder submit(Long id) {
        StockOutOrder order = stockOutOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.PENDING, OrderStatus.DRAFT);
        Long warehouseId = order.getWarehouseId();

        // ===== 【s3-1 锁定可售库存】提交审核时占用库存，防止超卖 =====
        List<StockOutItem> items = stockOutItemMapper.findByOrderId(id);
        for (StockOutItem item : items) {
            int affected = stockMapper.lockQuantity(item.getProductId(), warehouseId, item.getQuantity());
            if (affected == 0) {
                // 可售库存不足（可能被其他出库单占用），事务回滚，提交失败保持草稿
                throw new RuntimeException(
                        "商品ID " + item.getProductId() + " 可售库存不足，无法提交审核（可能已被其他出库单占用）");
            }
        }

        stockOutOrderMapper.updateStatus(id, OrderStatus.PENDING, null, null);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    @Override
    public StockOutOrder withdraw(Long id) {
        StockOutOrder order = stockOutOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.DRAFT, OrderStatus.PENDING);
        Long warehouseId = order.getWarehouseId();

        // ===== 【s3-1 释放锁定】撤回回草稿时释放占用的可售库存 =====
        List<StockOutItem> items = stockOutItemMapper.findByOrderId(id);
        for (StockOutItem item : items) {
            stockMapper.unlockQuantity(item.getProductId(), warehouseId, item.getQuantity());
        }

        stockOutOrderMapper.updateStatus(id, OrderStatus.DRAFT, null, null);
        order.setStatus(OrderStatus.DRAFT);
        return order;
    }

    @Override
    public StockOutOrder approve(Long id) {
        StockOutOrder order = stockOutOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.APPROVED, OrderStatus.PENDING);
        Long auditorId = getCurrentUserId();
        stockOutOrderMapper.updateStatus(id, OrderStatus.APPROVED, auditorId, LocalDateTime.now());
        order.setStatus(OrderStatus.APPROVED);
        order.setAuditorId(auditorId);
        order.setAuditTime(LocalDateTime.now());
        return order;
    }

    @Override
    public StockOutOrder reject(Long id) {
        StockOutOrder order = stockOutOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.DRAFT, OrderStatus.PENDING);
        Long warehouseId = order.getWarehouseId();

        // ===== 【s3-1 释放锁定】驳回回草稿时释放占用的可售库存 =====
        List<StockOutItem> items = stockOutItemMapper.findByOrderId(id);
        for (StockOutItem item : items) {
            stockMapper.unlockQuantity(item.getProductId(), warehouseId, item.getQuantity());
        }

        Long auditorId = getCurrentUserId();
        stockOutOrderMapper.updateStatus(id, OrderStatus.DRAFT, auditorId, LocalDateTime.now());
        order.setStatus(OrderStatus.DRAFT);
        order.setAuditorId(auditorId);
        order.setAuditTime(LocalDateTime.now());
        return order;
    }

    @Override
    public StockOutOrder post(Long id) {
        StockOutOrder order = stockOutOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.POSTED, OrderStatus.APPROVED);
        Long warehouseId = order.getWarehouseId();
        Long operatorId = order.getOperatorId();
        String orderNo = order.getOrderNo();

        // 读取全部出库明细（productId + quantity）
        List<StockOutItem> items = stockOutItemMapper.findByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("出库单明细为空，无法过账");
        }

        // ===== 校验并扣减库存 + 记录流水（这里才真正动库存）=====
        for (StockOutItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            // ① 先查当前库存
            Stock stock = stockMapper.findByProductAndWarehouse(productId, warehouseId);

            // ② 库存校验
            int beforeQty = 0;
            if (stock == null) {
                throw new RuntimeException("库存不存在，无法出库");
            }
            beforeQty = stock.getQuantity();
            if (beforeQty < quantity) {
                throw new RuntimeException(
                        "商品ID " + productId + " 库存不足，当前库存 " + beforeQty + "，出库 " + quantity);
            }

            // ③ 扣减库存（消耗锁定量：quantity 与 locked_quantity 同时减）
            // postDecrease 的 SQL 里带 quantity >= 出库数 且 locked_quantity >= 出库数 条件
            // 返回 0 = 库存/锁定不足
            int affected = stockMapper.postDecrease(productId, warehouseId, quantity);
            if (affected == 0) {
                throw new RuntimeException("商品ID " + productId + " 库存不足，扣减失败");
            }

            // ④ 记录出库流水
            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(warehouseId);
            log.setChangeType("OUT"); // 出库
            log.setChangeQuantity(quantity);
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty - quantity);
            log.setOrderNo(orderNo);
            log.setOperatorId(operatorId);
            stockLogMapper.insert(log);
        }

        // 最后更新状态到"已过账"
        stockOutOrderMapper.updateStatus(id, OrderStatus.POSTED, null, null);
        order.setStatus(OrderStatus.POSTED);
        return order;
    }

    @Override
    public StockOutOrder reverse(Long id, String remark) {
        // ===== 1. 校验单据：必须已过账 =====
        StockOutOrder order = stockOutOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.REVERSED, OrderStatus.POSTED);
        if (remark == null || remark.trim().isEmpty()) {
            throw new RuntimeException("必须填写红冲原因");
        }

        Long warehouseId = order.getWarehouseId();
        Long operatorId = getCurrentUserId();
        String origNo = order.getOrderNo();

        // ===== 2. 读取全部出库明细 =====
        List<StockOutItem> items = stockOutItemMapper.findByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("出库单明细为空，无法红冲");
        }

        // ===== 3. 反向回滚库存：出库红冲 = 加回库存，并写 OUT_REVERSE 流水 =====
        for (StockOutItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            Stock stock = stockMapper.findByProductAndWarehouse(productId, warehouseId);
            if (stock == null) {
                throw new RuntimeException("商品ID " + productId + " 库存不存在，无法红冲");
            }
            int beforeQty = stock.getQuantity();

            stockMapper.increaseQuantity(productId, warehouseId, quantity);

            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(warehouseId);
            log.setChangeType("OUT_REVERSE"); // 出库红冲反向流水
            log.setChangeQuantity(quantity);
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty + quantity);
            log.setOrderNo(origNo);
            log.setOperatorId(operatorId);
            stockLogMapper.insert(log);
        }

        // ===== 4. 生成独立红冲单（原单号+"R"，终态已红冲）=====
        StockOutOrder rev = new StockOutOrder();
        rev.setOrderNo(origNo + "R");
        rev.setWarehouseId(order.getWarehouseId());
        rev.setReceiver(order.getReceiver());
        rev.setOperatorId(order.getOperatorId());
        rev.setTotalAmount(order.getTotalAmount());
        rev.setStatus(OrderStatus.REVERSED);
        rev.setReverseOfNo(origNo);
        rev.setRemark(remark);
        stockOutOrderMapper.insert(rev);

        // ===== 5. 原单置已红冲 =====
        Long auditorId = getCurrentUserId();
        stockOutOrderMapper.updateStatus(id, OrderStatus.REVERSED, auditorId, LocalDateTime.now());

        return rev;
    }

    @Override
    public Map<String, Object> getPage(StockOutPageDTO dto) {
        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<StockOutOrderVO> list = stockOutOrderMapper.searchPage(dto, offset);
        long total = stockOutOrderMapper.countSearch(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());
        return result;
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