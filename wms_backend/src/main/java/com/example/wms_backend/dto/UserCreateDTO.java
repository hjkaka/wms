package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 新增用户入参
 */
@Data
public class UserCreateDTO {
    /** 登录名（唯一） */
    private String username;
    /** 明文密码（服务端 BCrypt 加密后入库） */
    private String password;
    /** 姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 角色：ADMIN / MANAGER / STAFF */
    private String role;
}