package com.example.wms_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存实体类
 * 对应数据库 stock 表
 * 记录每个商品在每个仓库的当前库存数量
 */
@Data
public class Stock {

    // 库存ID，主键，自增
    private Long id;

    // 商品ID，关联 product 表
    private Long productId;

    // 仓库ID，关联 warehouse 表
    private Long warehouseId;

    // 当前库存数量（实际在库，含被锁定的部分）
    // 入库时增加，出库时减少
    private Integer quantity;

    // 锁定/在途库存数量（s3-1）
    // 出库单"提交审核"时占用，过账时消耗，撤回/驳回时释放
    // 可售库存 = quantity - locked_quantity
    private Integer lockedQuantity;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}