package com.example.wms_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 出库请求参数 DTO
 * 前端创建出库单时提交的数据
 * DTO = 数据中转对象，只用来接收前端参数，不存表
 */
@Data
public class StockOutCreateDTO {

    // 仓库ID，从哪个仓库出库
    private Long warehouseId;

    // 收货人/领用人
    private String receiver;

    // 经办人ID
    private Long operatorId;

    // 备注
    private String remark;

    // 出库明细列表（可多条）
    private List<StockOutItemDTO> items;

    /**
     * 出库明细类
     * 用 @Data + static 定义，表示它是 StockOutCreateDTO 的"内部小类"
     * 每一条明细：出哪个商品、出多少、什么单价
     */
    @Data
    public static class StockOutItemDTO {
        // 商品ID
        private Long productId;
        // 出库数量
        private Integer quantity;
        // 单价
        private BigDecimal unitPrice;
    }
}