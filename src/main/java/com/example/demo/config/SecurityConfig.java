package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 6 配置 — SB2→SB3 最大的破坏性变化之一!
 *
 * ===================== SB2 (JDK8) 做法 =====================
 *
 *   @Configuration
 *   public class SecurityConfig extends WebSecurityConfigurerAdapter {
 *
 *       @Override
 *       protected void configure(HttpSecurity http) throws Exception {
 *           http
 *               .authorizeRequests()
 *                   .antMatchers("/api/public/**").permitAll()
 *                   .antMatchers("/api/admin/**").hasRole("ADMIN")
 *                   .anyRequest().authenticated()
 *                   .and()
 *               .formLogin().and()
 *               .csrf().disable();
 *       }
 *   }
 *
 * ===================== SB3 做法 =====================
 *
 * 1. WebSecurityConfigurerAdapter 已被删除! (这是最大的变化)
 * 2. 改用 @Bean SecurityFilterChain 的方式配置
 * 3. authorizeRequests() → authorizeHttpRequests() (方法名变了)
 * 4. antMatchers() → requestMatchers() (方法名变了)
 * 5. and() 链式调用 → Lambda DSL (更类型安全)
 * 6. csrf().disable() → csrf(AbstractHttpConfigurer::disable)
 * 7. SB3: import 全部是 jakarta.* 和 org.springframework.security.*
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * SB3 核心变化: 用 SecurityFilterChain Bean 替代 WebSecurityConfigurerAdapter
     *
     * Lambda DSL 优势:
     * - 编译时检查 (类型安全)
     * - 更好的 IDE 自动补全
     * - 不再需要 .and() 链式调用
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // SB3: 使用 Lambda DSL 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点: 注册、登录、Swagger、H2 控制台
                .requestMatchers(
                        "/api/public/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/h2-console/**"
                ).permitAll()
                // 管理员端点
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 其他端点需要认证
                .anyRequest().authenticated()
            )
            // SB3: CSRF 配置使用 Lambda
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        "/api/public/**",
                        "/h2-console/**",
                        "/swagger-ui/**"
                )
            )
            // SB3: REST API 使用无状态会话 (不使用 formLogin, 因为 formLogin 依赖 session)
            // 生产环境通常配合 JWT filter 实现认证
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // H2 控制台需要的 Headers 配置
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    /**
     * 密码编码器 — 与 SB2 相同, 但通过 @Bean 注册 (构造器注入到 Service)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
