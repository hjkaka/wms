package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 录入盘点实盘数量请求（s3-3）
 * 一条明细对应一次实盘录入。
 */
@Data
public class StockTakeItemUpdateDTO {
    // 盘点明细ID（stock_take_item.id，必填）
    private Long id;
    // 实盘数量；不录入(传 null)表示该项未盘点
    private Integer countedQuantity;
    // 盈亏原因备注
    private String remark;
}