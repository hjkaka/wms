package com.example.wms_backend.dto;

import lombok.Data;

@Data
public class WarehouseQueryDTO {
    private String name;
    private Integer status;

    private Integer pageNum=1;
    private Integer pageSize=10;

    public Integer getOffset(){
        if(pageNum==null||pageSize==null) {
            return 0;
        }
        return (pageNum-1)*pageSize;
    }
}
