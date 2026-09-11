package com.example.wms_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户响应（不含密码，避免泄露哈希）
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}