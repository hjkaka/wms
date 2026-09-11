package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 用户分页查询入参
 */
@Data
public class UserQueryDTO {
    /** 用户名（模糊） */
    private String username;
    /** 角色过滤 */
    private String role;
    /** 状态过滤 */
    private Integer status;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public Integer getOffset() {
        if (pageNum == null || pageSize == null) {
            return 0;
        }
        return (pageNum - 1) * pageSize;
    }
}