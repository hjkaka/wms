package com.example.wms_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存流水实体类
 * 对应数据库 stock_log 表
 * 记录每一次库存变动的详细情况
 * 用于审计和对账
 */
@Data
public class StockLog {

    // 流水ID，主键，自增
    private Long id;

    // 商品ID，关联 product 表
    private Long productId;

    // 仓库ID，关联 warehouse 表
    private Long warehouseId;

    // 变动类型：IN=入库 OUT=出库
    private String changeType;

    // 变动数量（正数）
    private Integer changeQuantity;

    // 变动前的库存数量
    private Integer beforeQuantity;

    // 变动后的库存数量
    private Integer afterQuantity;

    // 关联的单据编号（入库单号或出库单号）
    private String orderNo;

    // 操作人ID
    private Long operatorId;

    // 创建时间
    private LocalDateTime createTime;
}