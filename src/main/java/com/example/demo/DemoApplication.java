package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 3 应用入口
 *
 * SB3 变化:
 * - 与 SB2 基本相同, @SpringBootApplication 注解没变
 * - 内部扫描到的自动配置文件从 spring.factories 改为 AutoConfiguration.imports
 * - 最低要求 Java 17 (SB2 最低要求 Java 8)
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
