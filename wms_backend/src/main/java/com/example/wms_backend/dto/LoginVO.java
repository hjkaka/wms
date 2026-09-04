// 包声明
package com.example.wms_backend.dto;

import lombok.Data;

@Data
public class LoginVO {

    // JWT Token（前端要存起来，以后每次请求都带上）
    private String token;

    // 用户ID
    private Long userId;

    // 用户名
    private String username;

    // 真实姓名
    private String realName;

    // 角色
    private String role;
}