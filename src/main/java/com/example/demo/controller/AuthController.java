package com.example.demo.controller;

import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.types.ApiResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 — 注册端点
 *
 * SB3 变化:
 * - import jakarta.validation.Valid (不是 javax.validation.Valid)
 * - import jakarta.servlet.http.* (不是 javax.servlet.http.*)
 * - 构造器注入替代 @Autowired
 */
@RestController
@RequestMapping("/api/public")
public class AuthController {

    // SB3: 构造器注入, 无 @Autowired
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * 演示:
     * 1. Record 作为 @RequestBody 参数 + @Valid 校验
     * 2. ApiResponse (sealed interface) 作为返回类型
     * 3. ResponseEntity 包装
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserCreateRequest request) {

        ApiResponse<UserResponse> result = userService.createUser(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 登录端点 (简化版)
     *
     * 注意: 实际项目中, Spring Security 的表单登录或 JWT 会处理登录逻辑.
     * 这里仅为演示 SB3 的端点配置.
     * SessionCreationPolicy.STATELESS 模式下, 此端点仅作占位.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login() {
        return ResponseEntity.ok(
                ApiResponse.success("请使用 Spring Security 表单登录或配置 JWT")
        );
    }
}
