package com.example.wms_backend.integration;

import com.example.wms_backend.dto.LoginDTO;
import com.example.wms_backend.dto.LoginVO;
import com.example.wms_backend.service.AuthService;
import com.example.wms_backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证 JWT 集成测试：真实库中的 admin(bcrypt) 登录、JWT 生成与解析校验。
 */
class AuthJwtIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    AuthService authService;
    @Autowired
    JwtUtil jwtUtil;

    @Test
    void admin登录成功返回token且能解析() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        LoginVO vo = authService.login(dto);
        assertNotNull(vo.getToken(), "登录应返回 token");
        assertEquals("ADMIN", vo.getRole(), "角色应为 ADMIN");
        assertEquals("admin", vo.getUsername(), "用户名应为 admin");
        assertNotNull(vo.getUserId(), "应返回 userId");
    }

    @Test
    void token可解析出用户关键信息() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        LoginVO vo = authService.login(dto);
        String token = vo.getToken();

        Claims claims = jwtUtil.parseToken(token);
        // subject = userId
        assertEquals(String.valueOf(vo.getUserId()), claims.getSubject(), "subject 应为 userId");
        assertEquals("admin", claims.get("username", String.class), "claim 应含 username");
        assertEquals("ADMIN", claims.get("role", String.class), "claim 应含 role");
        assertEquals(vo.getUserId(), jwtUtil.getUserIdFromToken(token), "getUserIdFromToken 应还原 userId");
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token), "getRoleFromToken 应还原 role");
    }

    @Test
    void 密码错误登录失败() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong-password");
        assertThrows(RuntimeException.class,
                () -> authService.login(dto),
                "密码错误应抛异常");
    }

    @Test
    void 不存在的用户登录失败() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("ghost_user_不存在");
        dto.setPassword("123456");
        assertThrows(RuntimeException.class,
                () -> authService.login(dto),
                "不存在用户应抛异常");
    }
}