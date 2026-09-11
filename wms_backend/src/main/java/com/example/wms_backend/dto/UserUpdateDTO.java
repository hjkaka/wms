package com.example.wms_backend.dto;

import lombok.Data;

/**
 * 修改用户入参
 * 说明：用户名不可改；password 留空表示不改，填了新值才用 BCrypt 重奖并更新。
 */
@Data
public class UserUpdateDTO {
    /** 姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 角色：ADMIN / MANAGER / STAFF */
    private String role;
    /** 可选：新密码（明文，空串/不传则保持原密码） */
    private String password;
}