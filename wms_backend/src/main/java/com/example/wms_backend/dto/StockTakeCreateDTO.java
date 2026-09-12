package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 创建盘点单请求参数（s3-3）
 * 只需指定仓库，系统自动纳入该仓库所有有库存商品作为账面快照明细。
 */
@Data
public class StockTakeCreateDTO {
    // 盘点仓库ID（必填）
    private Long warehouseId;
    // 备注
    private String remark;
}