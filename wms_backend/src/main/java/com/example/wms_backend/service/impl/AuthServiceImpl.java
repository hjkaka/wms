package com.example.wms_backend.service.impl;

import com.example.wms_backend.config.LoginAttemptService;
import com.example.wms_backend.dto.LoginDTO;
import com.example.wms_backend.dto.LoginVO;
import com.example.wms_backend.entity.SysUser;
import com.example.wms_backend.exception.LoginLockedException;
import com.example.wms_backend.mapper.SysUserMapper;
import com.example.wms_backend.service.AuthService;
import com.example.wms_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // s1-5 登录防爆破：用户名+IP 滑动窗口失败计数
    @Autowired
    private LoginAttemptService loginAttemptService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String ip = clientIp();

        // 第0步（s1-5）：若该用户名+IP已被锁定，直接拒绝
        if (loginAttemptService.isBlocked(username, ip)) {
            throw new LoginLockedException("登录失败次数过多，账号已临时锁定，请15分钟后再试");
        }

        // 第1步：根据用户名查数据库
        SysUser user = sysUserMapper.findByUsername(username);

        // 第2步：判断用户是否存在
        if (user == null) {
            loginAttemptService.recordFailure(username, ip);
            throw new RuntimeException("用户名不存在");
        }

        // 第3步：判断账号是否被禁用
        if (user.getStatus() == 0) {
            loginAttemptService.recordFailure(username, ip);
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 第4步：验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(username, ip);
            throw new RuntimeException("密码错误");
        }

        // 第5步：登录成功，清零该用户名+IP的失败计数
        loginAttemptService.recordSuccess(username, ip);

        // 第6步：生成 JWT Token
        String token = jwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            user.getRole()
        );

        // 第7步：组装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());

        return loginVO;
    }

    /** 取客户端IP：优先 X-Forwarded-For（反向代理场景），否则用直接连接地址 */
    private String clientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String xff = req.getHeader("X-Forwarded-For");
                if (xff != null && !xff.trim().isEmpty()) {
                    return xff.split(",")[0].trim();
                }
                return req.getRemoteAddr();
            }
        } catch (Exception ignore) {
            // 拿不到请求信息就返回空串（退化为仅按用户名计数）
        }
        return "";
    }
}