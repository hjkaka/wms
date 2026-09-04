// 第1行：包声明
package com.example.wms_backend.controller;

// 第2-7行：导入需要的类
import com.example.wms_backend.common.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController：标记为控制器
@RestController
// @RequestMapping：设置路径前缀
@RequestMapping("/api/test")
public class TestController {

    /**
     * 测试1：获取当前登录用户信息
     *
     * 请求方式：GET
     * 路径：/api/test/info
     *
     * 这个接口需要带 Token 才能访问
     * 用于演示 JwtFilter 解析 Token 后，如何在 Controller 获取用户信息
     */
    @GetMapping("/info")
    public Result<?> getCurrentUserInfo() {

        // ====== 从 Security 上下文获取当前认证信息 ======
        // SecurityContextHolder：存放当前请求的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 如果没有认证信息（理论上不会到这里，因为 Security 已经拦截了）
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }

        // ====== 从认证信息中提取用户信息 ======
        // getPrincipal()：获取我们在 JwtFilter 里存的用户ID
        Long userId = (Long) authentication.getPrincipal();

        // getAuthorities()：获取我们在 JwtFilter 里存的角色权限
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // ====== 组装返回数据 ======
        // 用一个简单的 Map 返回
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("userId", userId);
        info.put("role", role);
        info.put("message", "你已经通过了 JWT 验证！");

        return Result.success(info);
    }

    /**
     * 测试2：一个简单的受保护接口
     *
     * 请求方式：GET
     * 路径：/api/test/protected
     *
     * 不带 Token 访问会返回 401
     * 带有效 Token 访问会返回成功
     */
    @GetMapping("/protected")
    public Result<String> protectedEndpoint() {
        return Result.success("这是受保护的内容，只有登录用户才能看到！");
    }
}