package com.example.demo.http;

import com.example.demo.dto.UserResponse;
import com.example.demo.model.types.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * SB3 新特性: HTTP Interface Client (声明式 HTTP 客户端)
 *
 * ===================== SB2 (JDK8) 做法 =====================
 *
 *   // 使用 RestTemplate (命令式)
 *   RestTemplate restTemplate = new RestTemplate();
 *   UserResponse user = restTemplate.getForObject("/api/users/1", UserResponse.class);
 *
 *   // 或使用 WebClient (响应式)
 *   WebClient client = WebClient.create("http://localhost:8080");
 *   UserResponse user = client.get().uri("/api/users/1")
 *       .retrieve().bodyToMono(UserResponse.class).block();
 *
 * ===================== SB3 做法 =====================
 *
 * 声明式接口: 定义方法签名, Spring 自动生成实现 (类似 OpenFeign)
 * - @HttpExchange: 声明 HTTP 交换的基础配置
 * - @GetExchange: 声明 GET 请求
 * - 方法参数自动映射为 URL 路径变量或查询参数
 * - 返回值自动反序列化
 *
 * 使用方式: 在配置类中通过 HttpServiceProxyFactory 创建代理 Bean
 * (见 config/AppConfig.java)
 */
@HttpExchange(url = "/api", accept = "application/json")
public interface ExternalApiClient {

    /** 获取用户列表 */
    @GetExchange("/users")
    ApiResponse<List<UserResponse>> getUsers();

    /** 根据 ID 获取用户 */
    @GetExchange("/users/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable Long id);
}
