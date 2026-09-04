package com.example.wms_backend.controller;

import com.example.wms_backend.common.Result;
import com.example.wms_backend.dto.LoginDTO;
import com.example.wms_backend.dto.LoginVO;
import com.example.wms_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController：标记为控制器，方法返回值自动转 JSON
@RestController
// @RequestMapping：设置这个控制器的公共路径前缀
// 所有这个类里的接口都会自动加上 /api/auth 前缀
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    /**
     * 登录接口
     *
     * 请求方式：POST（因为要提交数据，不是查询）
     * 完整路径：/api/auth/login
     *
     * 前端请求体（JSON 格式）：
     *   {
     *     "username": "admin",
     *     "password": "123456"
     *   }
     *
     * 成功返回：
     *   {
     *     "code": 200,
     *     "message": "success",
     *     "data": {
     *       "token": "eyJhbGciOi...",
     *       "userId": 1,
     *       "username": "admin",
     *       "realName": "管理员",
     *       "role": "ADMIN"
     *     }
     *   }
     *
     * 失败返回（由 GlobalExceptionHandler 处理）：
     *   {
     *     "code": 400,
     *     "message": "用户名不存在",
     *     "data": null
     *   }
     */
    @PostMapping("/login")
    public Result<LoginVO> login(  @RequestBody LoginDTO loginDTO)
    {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }




}