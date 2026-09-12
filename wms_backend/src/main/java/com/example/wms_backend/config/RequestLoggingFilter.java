package com.example.wms_backend.config;

import com.example.wms_backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 请求访问日志（4.4 日志分级 + 请求日志）
 *
 * <p>用独立 logger "REQUEST" 写 {@code logs/wms-request.log}（logback 配置里 additivity=false，不污染主日志）。
 * 一行一条，字段用 | 分隔：时间|方法|URI|状态码|耗时ms|客户端IP|操作人(userId)。</p>
 *
 * <p>{@code @Order(-101)} 让本过滤器排在 Spring Security 过滤链（默认 -100）之前，即使请求被安全机制
 * 拦截（401/403）也能记录，实现完整的访问审计（含失败登录/越权尝试的 IP 溯源）。</p>
 *
 * <p>操作人：在入站时从 Authorization Bearer token 用 {@link JwtUtil} 解析（JWT 无状态，安全链运行前
 * SecurityContext 尚未填充）。token 缺失或无效则记 "-"。best-effort，日志异常不影响业务。</p>
 */
@Component
@Order(-101)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger REQUEST_LOG = LoggerFactory.getLogger("REQUEST");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final JwtUtil jwtUtil;

    public RequestLoggingFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        Long operatorId = resolveOperator(request);
        try {
            chain.doFilter(request, response);
        } finally {
            try {
                long cost = System.currentTimeMillis() - start;
                String uri = request.getRequestURI();
                if (request.getQueryString() != null) {
                    uri += "?" + request.getQueryString();
                }
                REQUEST_LOG.info("{}|{}|{}|{}|{}ms|{}|{}",
                        LocalDateTime.now().format(FMT),
                        request.getMethod(),
                        uri,
                        response.getStatus(),
                        cost,
                        clientIp(request),
                        operatorId == null ? "-" : operatorId);
            } catch (Exception ignore) {
                // 记录请求日志失败不阻断业务
            }
        }
    }

    /** 从 Bearer token 解析操作人 userId；缺失/无效返回 null */
    private Long resolveOperator(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        String token = auth.substring("Bearer ".length());
        if (token.isEmpty()) {
            return null;
        }
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null; // 过期/非法 token，按未登录记录
        }
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}