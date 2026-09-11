package com.example.wms_backend.service.impl;

import com.example.wms_backend.constant.OrderStatus;
import com.example.wms_backend.dto.StockInCreateDTO;
import com.example.wms_backend.dto.StockInPageDTO;
import com.example.wms_backend.vo.StockInOrderVO;
import com.example.wms_backend.entity.*;
import com.example.wms_backend.mapper.*;
import com.example.wms_backend.service.StockInService;
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
 * @Service：标记为 Service 类
 * @Transactional：声明这个类的方法都在事务中执行
 * 事务 = 一系列操作要么全部成功，要么全部失败
 * 比如：入库单插入成功，但库存更新失败 → 整个操作回滚（入库单也撤销）
 *
 * ===== s2-2 审核流重构 =====
 * 状态机：草稿(0) → 待审(1) → 已审(2) → 已过账(3)
 * 关键约束：库存只在"过账(post)"阶段才会变动
 * 权限：STAFF 创建+提交+撤回；MANAGER/ADMIN 审核(approve/reject)+过账(post)
 */
@Service
@Transactional  // 事务管理
public class StockInServiceImpl implements StockInService {

    // ===== 注入 5 个 Mapper =====
    @Autowired
    private StockInOrderMapper stockInOrderMapper;

    @Autowired
    private StockInItemMapper stockInItemMapper;

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
        // 异常：未登录则抛出（不会走到这里，因为 Security 已经拦了 401）
        throw new RuntimeException("获取当前用户失败，请检查登录状态");
    }

    /**
     * 校验状态转换合法性
     * 根据状态机规则：只有特定原状态才能转到目标状态
     * @param currentStatus 当前单据状态
     * @param targetStatus 目标状态
     * @param allowed 允许的原状态数组
     */
    private void checkTransition(Integer currentStatus, int targetStatus, int... allowed) {
        for (int a : allowed) {
            if (currentStatus == a) {
                return;
            }
        }
        String msg = String.format("状态不合法：当前%d 无法转到%d", currentStatus, targetStatus);
        throw new RuntimeException(msg);
    }

    // ============ 业务接口实现 =============

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
        order.setStatus(OrderStatus.DRAFT); // 新建 = 草稿（不动库存）
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

        // ===== 【s2-2 改变】这里不再立刻更新库存，只存草稿 =====
        // 库存更新 & 流水记录推迟到 "过账(post)" 阶段执行

        // 返回创建成功的入库单
        return order;
    }

    @Override
    public StockInOrder submit(Long id) {
        // 查询单据
        StockInOrder order = stockInOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在，id=" + id);
        }
        // 状态校验：只能草稿(0)提交
        checkTransition(order.getStatus(), OrderStatus.PENDING, OrderStatus.DRAFT);
        // 更新状态：草稿 → 待审
        stockInOrderMapper.updateStatus(id, OrderStatus.PENDING, null, null);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    @Override
    public StockInOrder withdraw(Long id) {
        // 查询单据
        StockInOrder order = stockInOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在，id=" + id);
        }
        // 状态校验：只能待审(1)撤回
        checkTransition(order.getStatus(), OrderStatus.DRAFT, OrderStatus.PENDING);
        // 更新状态：待审 → 草稿
        stockInOrderMapper.updateStatus(id, OrderStatus.DRAFT, null, null);
        order.setStatus(OrderStatus.DRAFT);
        return order;
    }

    @Override
    public StockInOrder approve(Long id) {
        // 查询单据
        StockInOrder order = stockInOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在，id=" + id);
        }
        // 状态校验：只能待审(1)审核通过
        checkTransition(order.getStatus(), OrderStatus.APPROVED, OrderStatus.PENDING);
        // 当前审核人（当前登录用户）
        Long auditorId = getCurrentUserId();
        // 更新状态：待审 → 已审，记录审核人+时间
        stockInOrderMapper.updateStatus(id, OrderStatus.APPROVED, auditorId, LocalDateTime.now());
        order.setStatus(OrderStatus.APPROVED);
        order.setAuditorId(auditorId);
        order.setAuditTime(LocalDateTime.now());
        return order;
    }

    @Override
    public StockInOrder reject(Long id) {
        // 查询单据
        StockInOrder order = stockInOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在，id=" + id);
        }
        // 状态校验：只能待审(1)驳回（驳回→草稿）
        checkTransition(order.getStatus(), OrderStatus.DRAFT, OrderStatus.PENDING);
        // 当前驳回人 = 当前登录用户
        Long auditorId = getCurrentUserId();
        // 更新状态：待审 → 草稿，记录驳回人+时间
        stockInOrderMapper.updateStatus(id, OrderStatus.DRAFT, auditorId, LocalDateTime.now());
        order.setStatus(OrderStatus.DRAFT);
        order.setAuditorId(auditorId);
        order.setAuditTime(LocalDateTime.now());
        return order;
    }

    @Override
    public StockInOrder post(Long id) {
        // 查询单据
        StockInOrder order = stockInOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在，id=" + id);
        }
        // 状态校验：只能已审(2)过账
        checkTransition(order.getStatus(), OrderStatus.POSTED, OrderStatus.APPROVED);
        Long warehouseId = order.getWarehouseId();
        Long operatorId = order.getOperatorId();
        String orderNo = order.getOrderNo();

        // 读取全部入库明细（productId + quantity）
        List<StockInItem> items = stockInItemMapper.findByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("入库单明细为空，无法过账");
        }

        // ===== 更新库存 + 记录流水（这里才真正动库存）=====
        for (StockInItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            // 查询当前库存
            Stock stock = stockMapper.findByProductAndWarehouse(productId, warehouseId);

            int beforeQty = 0; // 变动前的库存数量
            if (stock == null) {
                // 库存表中没有这条记录 → 新增
                stock = new Stock();
                stock.setProductId(productId);
                stock.setWarehouseId(warehouseId);
                stock.setQuantity(quantity);
                stock.setLockedQuantity(0); // s3-1: 新建库存默认无锁定
                stockMapper.insert(stock);
                beforeQty = 0;
            } else {
                // 已有记录 → 增加数量
                beforeQty = stock.getQuantity();
                stockMapper.increaseQuantity(productId, warehouseId, quantity);
            }

            // 记录库存流水
            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(warehouseId);
            log.setChangeType("IN"); // 入库
            log.setChangeQuantity(quantity);
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty + quantity);
            log.setOrderNo(orderNo);
            log.setOperatorId(operatorId);
            stockLogMapper.insert(log);
        }

        // 最后更新状态到"已过账"
        stockInOrderMapper.updateStatus(id, OrderStatus.POSTED, null, null);
        order.setStatus(OrderStatus.POSTED);
        return order;
    }

    @Override
    public StockInOrder reverse(Long id, String remark) {
        // ===== 1. 校验单据：必须已过账 =====
        StockInOrder order = stockInOrderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在，id=" + id);
        }
        checkTransition(order.getStatus(), OrderStatus.REVERSED, OrderStatus.POSTED);
        if (remark == null || remark.trim().isEmpty()) {
            throw new RuntimeException("必须填写红冲原因");
        }

        Long warehouseId = order.getWarehouseId();
        Long operatorId = getCurrentUserId();
        String origNo = order.getOrderNo();

        // ===== 2. 读取全部入库明细 =====
        List<StockInItem> items = stockInItemMapper.findByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("入库单明细为空，无法红冲");
        }

        // ===== 3. 反向回滚库存：入库红冲 = 扣减库存（带防负），并写 IN_REVERSE 流水 =====
        for (StockInItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            Stock stock = stockMapper.findByProductAndWarehouse(productId, warehouseId);
            if (stock == null) {
                throw new RuntimeException("商品ID " + productId + " 库存不存在，无法红冲");
            }
            int beforeQty = stock.getQuantity();

            // decreaseQuantity 带 quantity>=qty 原子条件，返回 0 = 库存不足
            int affected = stockMapper.decreaseQuantity(productId, warehouseId, quantity);
            if (affected == 0) {
                throw new RuntimeException(
                        "商品ID " + productId + " 库存不足，无法红冲（当前库存 " + beforeQty + ", 需扣减 " + quantity + "）");
            }

            StockLog log = new StockLog();
            log.setProductId(productId);
            log.setWarehouseId(warehouseId);
            log.setChangeType("IN_REVERSE"); // 入库红冲反向流水
            log.setChangeQuantity(quantity);
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(beforeQty - quantity);
            log.setOrderNo(origNo);
            log.setOperatorId(operatorId);
            stockLogMapper.insert(log);
        }

        // ===== 4. 生成独立红冲单（原单号+"R"，终态已红冲）=====
        StockInOrder rev = new StockInOrder();
        rev.setOrderNo(origNo + "R");
        rev.setWarehouseId(order.getWarehouseId());
        rev.setSupplier(order.getSupplier());
        rev.setOperatorId(order.getOperatorId());
        rev.setTotalAmount(order.getTotalAmount());
        rev.setStatus(OrderStatus.REVERSED);
        rev.setReverseOfNo(origNo);
        rev.setRemark(remark);
        stockInOrderMapper.insert(rev);

        // ===== 5. 原单置已红冲 =====
        Long auditorId = getCurrentUserId();
        stockInOrderMapper.updateStatus(id, OrderStatus.REVERSED, auditorId, LocalDateTime.now());

        return rev;
    }

    @Override
    public Map<String, Object> getPage(StockInPageDTO dto) {
        // 计算跳过条数（第几页 * 每页条数）
        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        // 查当前页数据
        List<StockInOrderVO> list = stockInOrderMapper.searchPage(dto, offset);
        // 查总条数
        long total = stockInOrderMapper.countSearch(dto);
        // 组装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());
        return result;
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