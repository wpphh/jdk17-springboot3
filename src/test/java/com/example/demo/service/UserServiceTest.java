package com.example.demo.service;

import com.example.demo.dto.PageRequest;
import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.types.ApiResponse;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Service 层测试 — 使用 Mockito (纯单元测试, 不启动 Spring)
 *
 * SB3 测试变化:
 * - @ExtendWith(MockitoExtension.class) 用法与 SB2 相同
 * - 演示 ApiResponse (sealed interface) 的 pattern matching 断言
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_existingUser_returnsSuccess() {
        // Arrange
        User user = new User("testuser", "test@example.com", "pass");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        ApiResponse<UserResponse> result = userService.getUserById(1L);

        // Assert: 使用 instanceof pattern matching 验证 sealed type 结果
        // JDK17: instanceof 自动绑定子类型变量 (switch on sealed type 需要 JDK21)
        assertThat(result).isInstanceOf(ApiResponse.Success.class);
        var success = (ApiResponse.Success<?>) result;
        assertThat(((UserResponse) success.data()).username()).isEqualTo("testuser");
        assertThat(((UserResponse) success.data()).email()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_nonExisting_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createUser_validRequest_returnsSuccess() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "newuser", "new@example.com", "password123", Set.of("USER"));

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(new Role("USER", "普通用户")));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        User savedUser = new User("newuser", "new@example.com", "encoded_password");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        ApiResponse<UserResponse> result = userService.createUser(request);

        // Assert
        assertThat(result).isInstanceOf(ApiResponse.Success.class);
        var success = (ApiResponse.Success<?>) result;
        assertThat(((UserResponse) success.data()).username()).isEqualTo("newuser");
        assertThat(success.message()).isEqualTo("用户创建成功");
    }
}
