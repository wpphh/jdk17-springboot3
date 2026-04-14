package com.example.demo.controller;

import com.example.demo.dto.PageRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.types.ApiResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理 REST 控制器
 *
 * SB3 变化总结:
 * 1. 构造器注入 (无 @Autowired)
 * 2. import jakarta.validation.Valid (非 javax)
 * 3. ApiResponse (sealed interface) 作为统一返回类型
 * 4. JDK17 特性在 Controller 层的应用
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    // SB3: final 字段 + 构造器注入
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户列表 (分页)
     *
     * 演示: Record DTO (PageRequest) 作为请求参数, 通过 toPageable() 转换
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @Valid PageRequest pageRequest) {

        ApiResponse<Page<UserResponse>> result = userService.getUserList(pageRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据 ID 获取用户
     *
     * 演示: PathVariable 获取参数, ApiResponse sealed interface 返回
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        ApiResponse<UserResponse> result = userService.getUserById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新用户状态
     *
     * 演示: @RequestBody + switch 表达式 (在 Service 层)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        ApiResponse<UserResponse> result = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        ApiResponse<Void> result = userService.deleteUser(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索用户
     *
     * 演示: 多个请求参数 + Text Block JPQL (在 Repository 层)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @Valid PageRequest pageRequest) {

        ApiResponse<Page<UserResponse>> result = userService.searchUsers(keyword, pageRequest);
        return ResponseEntity.ok(result);
    }
}
