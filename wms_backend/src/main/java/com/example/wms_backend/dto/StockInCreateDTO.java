package com.example.wms_backend.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockInCreateDTO {
    private Long warehouseId;
    private String supplier;
    private Long operatorId;
    private String remark;
    private List<StockInItemDTO> items;
    @Data
    public static class StockInItemDTO{
         private Long productId;
         private Integer quantity;
         private BigDecimal unitPrice;
     }

}
