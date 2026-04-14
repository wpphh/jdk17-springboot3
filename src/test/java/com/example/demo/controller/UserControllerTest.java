package com.example.demo.controller;

import com.example.demo.dto.PageRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.types.ApiResponse;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 层测试 — 使用 @WebMvcTest (切片测试)
 *
 * SB3 测试变化:
 * - @WebMvcTest 用法与 SB2 基本相同
 * - @MockBean 仍然可用 (SB3.4+ 推荐 @MockitoBean, 但 3.3 仍用 @MockBean)
 * - 演示 ProblemDetail 响应格式验证
 * - 演示 ApiResponse (sealed interface) 的 JSON 序列化
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private UserService userService;

    // ========== 辅助方法 ==========

    private UserResponse createSampleUserResponse() {
        return new UserResponse(
                1L, "testuser", "test@example.com",
                "ACTIVE", "活跃",
                Set.of("USER"),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // ========== GET /api/users/{id} ==========

    @Test
    @WithMockUser
    void getUser_existingId_returns200() throws Exception {
        UserResponse userResponse = createSampleUserResponse();
        when(userService.getUserById(1L))
                .thenReturn(ApiResponse.success(userResponse));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.statusDescription").value("活跃"));
    }

    @Test
    @WithMockUser
    void getUser_nonExistingId_returns404ProblemDetail() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("用户不存在: ID=999"));

        // SB3: 验证 ProblemDetail (RFC 7807) 格式
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("用户不存在: ID=999"));
    }

    // ========== GET /api/users ==========

    @Test
    @WithMockUser
    void listUsers_returnsPaginatedResults() throws Exception {
        UserResponse userResponse = createSampleUserResponse();
        PageImpl<UserResponse> page = new PageImpl<>(List.of(userResponse));
        when(userService.getUserList(any(PageRequest.class)))
                .thenReturn(ApiResponse.success(page));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    // ========== DELETE /api/users/{id} ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteUser_existingId_returns200() throws Exception {
        when(userService.deleteUser(1L))
                .thenReturn(ApiResponse.success(null, "用户删除成功"));

        mockMvc.perform(delete("/api/users/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户删除成功"));
    }

    // ========== PUT /api/users/{id}/status ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateStatus_returnsUpdatedUser() throws Exception {
        UserResponse userResponse = createSampleUserResponse();
        when(userService.updateUserStatus(eq(1L), eq("SUSPENDED")))
                .thenReturn(ApiResponse.success(userResponse, "状态更新成功"));

        mockMvc.perform(put("/api/users/1/status")
                        .param("status", "SUSPENDED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("状态更新成功"));
    }

    // ========== 未认证访问 ==========

    @Test
    void getUser_withoutAuth_returns401() throws Exception {
        // 无状态模式下, 未认证用户访问受保护端点返回 401
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }
}
