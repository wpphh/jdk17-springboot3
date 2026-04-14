package com.example.demo.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * SB3 新特性: 自定义自动配置
 *
 * ===================== SB2 (JDK8) 做法 =====================
 *
 * 注册方式: 在 META-INF/spring.factories 中添加:
 *   org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
 *     com.example.demo.autoconfig.AuditAutoConfiguration
 *
 * 注解: 使用 @Configuration
 *
 * ===================== SB3 做法 =====================
 *
 * 注册方式: 在 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 中添加:
 *   com.example.demo.autoconfig.AuditAutoConfiguration
 *
 * 注解: 使用 @AutoConfiguration (不是 @Configuration!)
 * - @AutoConfiguration 提供了更明确的语义
 * - 可以用 @AutoConfigureBefore/@AutoConfigureAfter 控制加载顺序
 * - spring.factories 中的自动配置注册在 SB3.0 中已废弃, SB3.2 中完全移除
 */
@AutoConfiguration
@ConditionalOnProperty(name = "app.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditAutoConfiguration {

    /**
     * 审计服务 Bean — 仅在容器中不存在 AuditService 时创建
     *
     * @ConditionalOnMissingBean: 如果用户自己定义了 AuditService Bean, 则不创建默认的
     * 这是自动配置的核心理念: 提供默认值, 但允许用户覆盖
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService() {
        return new AuditService();
    }

    /**
     * 简单的审计服务 — 演示自动配置的用途
     */
    public static class AuditService {

        public void log(String action, String detail) {
            // 实际项目中会写入数据库或发送到日志系统
            System.out.println("[AUDIT] %s: %s".formatted(action, detail));
        }
    }
}
