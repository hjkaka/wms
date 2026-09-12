package com.example.wms_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点明细实体
 * 对应 stock_take_item 表
 */
@Data
public class StockTakeItem {
    // 明细ID，自增主键
    private Long id;
    // 所属盘点单ID
    private Long orderId;
    // 商品ID
    private Long productId;
    // 账面数量（创建盘点单时的库存快照）
    private Integer systemQuantity;
    // 实盘数量（人工盘点录入）
    private Integer countedQuantity;
    // 盈亏差异 = counted - system（正=盘盈，负=盘亏）
    private Integer varianceQuantity;
    // 盈亏原因备注
    private String remark;
    private LocalDateTime createTime;
}