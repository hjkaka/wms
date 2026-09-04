package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.LoginDTO;
import com.example.wms_backend.dto.LoginVO;
import com.example.wms_backend.entity.SysUser;
import com.example.wms_backend.mapper.SysUserMapper;
import com.example.wms_backend.service.AuthService;
import com.example.wms_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {

        // 第1步：根据用户名查数据库
        SysUser user = sysUserMapper.findByUsername(loginDTO.getUsername());

        // 第2步：判断用户是否存在
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 第3步：判断账号是否被禁用
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 第4步：验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 第5步：生成 JWT Token
        String token = jwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            user.getRole()
        );

        // 第6步：组装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());

        return loginVO;
    }
}