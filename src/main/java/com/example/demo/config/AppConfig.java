package com.example.demo.config;

import com.example.demo.http.ExternalApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 应用配置 — 演示 SB3 新特性
 *
 * 1. RestClient (SB 3.2 新增) — 替代 RestTemplate 的同步 HTTP 客户端
 * 2. HTTP Interface Client 注册 — 通过 HttpServiceProxyFactory 创建代理
 *
 * ===================== SB2 (JDK8) 做法 =====================
 *   @Bean
 *   public RestTemplate restTemplate() {
 *       return new RestTemplate();
 *   }
 *
 * ===================== SB3 做法 =====================
 *   RestClient: 更现代的 API, 函数式构建, 更好的错误处理
 */
@Configuration
public class AppConfig {

    /**
     * SB3: RestClient — 新的同步 HTTP 客户端 (SB 3.2 引入)
     *
     * 相比 RestTemplate:
     * - 更流畅的 API: RestClient.get().uri(...).retrieve().body(...)
     * - 更好的错误处理: 自定义 ErrorHandler
     * - 与 HTTP Interface Client 配合使用
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    /**
     * 注册 ExternalApiClient 为 Spring Bean
     *
     * SB3 做法:
     * 1. 创建 RestClient (或 WebClient)
     * 2. 用 RestClientAdapter 包装
     * 3. 创建 HttpServiceProxyFactory
     * 4. 通过工厂创建接口代理
     *
     * 使用: 在 Service 中注入 ExternalApiClient 即可直接调用
     */
    @Bean
    public ExternalApiClient externalApiClient(RestClient restClient) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ExternalApiClient.class);
    }
}
