package com.example.wms_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存流水查询结果视图
 * 一行 = 一条库存变动记录，联表补商品名/编码、仓库名、操作人姓名
 */
@Data
public class StockLogVO {
    // 流水ID
    private Long id;
    // 商品名（联表 product）
    private String productName;
    // 商品编码
    private String productCode;
    // 仓库名（联表 warehouse）
    private String warehouseName;
    // 变动类型：IN=入库 OUT=出库
    private String changeType;
    // 变动数量（正数，IN 增加 / OUT 减少，前端按类型展示符号）
    private Integer changeQuantity;
    // 变动前库存
    private Integer beforeQuantity;
    // 变动后库存
    private Integer afterQuantity;
    // 关联单据号
    private String orderNo;
    // 操作人姓名（联表 sys_user）
    private String operatorName;
    // 变动时间
    private LocalDateTime createTime;
}