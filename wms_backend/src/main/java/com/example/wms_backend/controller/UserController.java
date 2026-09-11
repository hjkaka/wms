package com.example.wms_backend.controller;

import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.UserCreateDTO;
import com.example.wms_backend.dto.UserQueryDTO;
import com.example.wms_backend.dto.UserUpdateDTO;
import com.example.wms_backend.service.UserManageService;
import com.example.wms_backend.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理接口（仅 ADMIN）
 * 路径：/api/user/**
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserManageService userManageService;

    // ===== 新增用户 =====
    // POST /api/user
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> createUser(@RequestBody UserCreateDTO dto) {
        return Result.success(userManageService.createUser(dto));
    }

    // ===== 修改用户 =====
    // PUT /api/user/{id}（密码留空不改）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        return Result.success(userManageService.updateUser(id, dto));
    }

    // ===== 启停用户 =====
    // PUT /api/user/{id}/status?status=0|1
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userManageService.changeStatus(id, status);
        return Result.success();
    }

    // ===== 分页查询用户 =====
    // GET /api/user/page
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> pageUsers(UserQueryDTO query) {
        return Result.success(userManageService.pageUsers(query));
    }
}