package com.example.wms_backend.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private  String name;
    private Long categoryId;
    private Integer status;
    private Integer pageNum=1;
    private Integer pageSize = 10;
    public Integer getOffset() {
        // 如果 pageNum 或 pageSize 为 null（理论上不会发生，因为有默认值）
        // 但为了代码健壮性，还是做个判断，防止空指针异常
        if (pageNum == null || pageSize == null) {
            return 0; // 返回 0，表示从第一条开始查
        }
        // 套用公式计算
        return (pageNum - 1) * pageSize;
    }
}
