package com.example.demo.service.impl;

import com.example.demo.dto.PageRequest;
import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.types.ApiResponse;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户服务实现
 *
 * SB3 重要变化: 构造器注入 (取代 @Autowired 字段注入)
 *
 * SB2 (JDK8) 常见做法:
 *   @Autowired
 *   private UserRepository userRepository;       // 字段注入
 *   @Autowired
 *   private RoleRepository roleRepository;
 *
 * SB3 推荐做法:
 *   - 使用构造器注入 (单构造器时 @Autowired 可省略)
 *   - 字段声明为 final (确保不可变, 依赖明确)
 *   - SB3 中单构造器自动注入, 无需任何注解
 *
 * 为什么构造器注入更好:
 * 1. 依赖明确可见 (不是隐藏在字段里)
 * 2. 字段可以是 final (不可变)
 * 3. 便于单元测试 (直接传入 mock 对象)
 * 4. 避免循环依赖问题
 */
@Service
public class UserServiceImpl implements UserService {

    // SB3: 所有依赖声明为 final, 通过构造器注入
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * SB3 重要: 单构造器时, Spring 自动注入, 不需要 @Autowired 注解!
     * SB2 常见: 每个构造器参数上加 @Autowired
     */
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApiResponse<Page<UserResponse>> getUserList(PageRequest pageRequest) {
        Page<User> users = userRepository.findAll(pageRequest.toPageable());
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<UserResponse> getUserById(Long id) {
        // JDK17: Optional + orElseThrow, 异常信息使用 text block 思想 (formatted)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "用户不存在: ID=%d".formatted(id)));

        return ApiResponse.success(UserResponse.fromEntity(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在: " + request.username());
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_EXISTS", "邮箱已被使用: " + request.email());
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        // SB3: 密码加密 (与 SB2 相同, 但通过构造器注入 PasswordEncoder)
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(User.Status.ACTIVE);

        // 分配角色
        Set<String> roleNames = request.roles() != null ? request.roles() : Set.of("USER");
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "角色不存在: %s".formatted(roleName)));
            user.addRole(role);
        }

        User saved = userRepository.save(user);
        return ApiResponse.success(UserResponse.fromEntity(saved), "用户创建成功");
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "用户不存在: ID=%d".formatted(id)));

        // JDK17: switch 表达式用于状态转换 (替代 if-else if 链)
        User.Status newStatus = switch (status.toUpperCase()) {
            case "ACTIVE"    -> User.Status.ACTIVE;
            case "SUSPENDED" -> User.Status.SUSPENDED;
            case "DELETED"   -> User.Status.DELETED;
            default -> throw new BusinessException("INVALID_STATUS",
                    "无效状态: %s (允许: ACTIVE, SUSPENDED, DELETED)".formatted(status));
        };

        user.setStatus(newStatus);
        User saved = userRepository.save(user);
        return ApiResponse.success(UserResponse.fromEntity(saved), "状态更新成功");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("用户不存在: ID=%d".formatted(id));
        }
        userRepository.deleteById(id);
        return ApiResponse.success(null, "用户删除成功");
    }

    @Override
    public ApiResponse<Page<UserResponse>> searchUsers(String keyword, PageRequest pageRequest) {
        Page<User> users = userRepository.searchUsers(keyword, pageRequest.toPageable());
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ApiResponse.success(response);
    }
}
