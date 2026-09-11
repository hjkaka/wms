package com.example.wms_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 生产环境敏感项 fail-fast 守卫（4.1 配置分离）
 *
 * <p>问题背景：{@code spring.datasource.password=${DB_PASSWORD}} 若直接写在 properties 里，
 * Spring Boot 的 Binder 对缺失的环境变量占位符不会强制失败（会以空串继续，HikariCP 又是惰性连接），
 * 导致"忘了配 DB_PASSWORD 也能启动、误连到错误库"。
 *
 * <p>解决：本类仅在 prod profile 生效，用 {@code @Value} 显式引用 ${DB_PASSWORD}。
 * {@code @Value} 走 Spring 的 resolveRequiredPlaceholders，占位符缺键时会抛
 * IllegalArgumentException("Could not resolve placeholder 'DB_PASSWORD'")，从而在启动阶段直接失败，
 * 防止生产误连。字段值本身不被使用，仅作强制校验用。
 *
 * <p>dev / test profile 不加载本类，不影响本地开发。
 */
@Configuration
@Profile("prod")
public class ProdSecretGuard {

    // 仅用于启动期占位符校验；非 null 但不真正被业务使用
    @SuppressWarnings("unused")
    @Value("${DB_PASSWORD}")
    private String dbPassword;
}