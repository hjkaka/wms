package com.example.wms_backend.config;

import com.example.wms_backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration       // 告诉 Spring：这是一个配置类，里面的内容是项目配置
@EnableWebSecurity   // 开启 Spring Security 功能
public class SecurityConfig {


    // 注入JwtFilter
   @Autowired
   private JwtFilter jwtFilter;

   @Bean
   public PasswordEncoder passwordEncoder(){
       return new BCryptPasswordEncoder();
   }
    /**
     * 配置安全过滤链
     * 这是 Spring Security 2.7+ 的新写法（用 Bean 方式）
     * 老版本是继承 WebSecurityConfigurerAdapter，但 2.7 开始已废弃
     *
     * @param http HttpSecurity 对象，用来配置安全规则
     * @return SecurityFilterChain 过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 第1步：关闭 CSRF 防护
            // CSRF 是防止跨站请求伪造的，前后端分离项目不需要（因为用 JWT 而不是 Cookie）
            .csrf().disable()

            // 第2步：设置 Session 策略为无状态
            // STATELESS = 不创建和使用 Session（我们用 JWT 认证，不需要 Session）
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // 第3步：配置 URL 的权限规则
            .authorizeRequests()
                // /api/hello 所有人都能访问（不需要登录）
                .antMatchers("/api/hello","/api/auth/login","/api/product/**","/api/stockin/**","/api/stockout/**","/api/stock/**").permitAll()
                // 其他所有接口都需要登录才能访问
                .anyRequest().authenticated()
            // ====== 添加 JWT 过滤器 ======
            // 在 UsernamePasswordAuthenticationFilter 之前执行
            // 这样 JwtFilter 可以先解析 Token，设置认证信息
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 构建并返回过滤链
        return http.build();
    }
}