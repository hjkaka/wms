package com.example.wms_backend.service;

import com.example.wms_backend.dto.UserCreateDTO;
import com.example.wms_backend.dto.UserQueryDTO;
import com.example.wms_backend.dto.UserUpdateDTO;
import com.example.wms_backend.vo.UserVO;

import java.util.Map;

/**
 * 用户管理服务（仅 ADMIN 可调用）
 */
public interface UserManageService {

    /** 新增用户（密码 BCrypt 加密后入库，默认启用） */
    UserVO createUser(UserCreateDTO dto);

    /** 修改用户（姓名/手机/角色；密码留空不改） */
    UserVO updateUser(Long id, UserUpdateDTO dto);

    /** 启停用户（保护：不能停用 admin、不能停用当前登录账号） */
    void changeStatus(Long id, Integer status);

    /** 分页查询用户 */
    Map<String, Object> pageUsers(UserQueryDTO query);
}