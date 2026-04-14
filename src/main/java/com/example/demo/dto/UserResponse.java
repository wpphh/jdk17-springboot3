package com.example.demo.dto;

import com.example.demo.model.Role;
import com.example.demo.model.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户响应 DTO — Record
 *
 * 演示: record 中使用 switch 表达式 + static factory method
 *
 * SB2 (JDK8) 做法: 手写 UserResponse 的构建逻辑, 在 Service 或 Controller 中逐字段赋值
 * JDK17 做法: record 的静态工厂方法 + switch 表达式, 一个方法完成转换
 */
public record UserResponse(

        Long id,
        String username,
        String email,
        String status,
        String statusDescription,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    /**
     * 从 User 实体转换为 UserResponse — 使用 switch 表达式
     *
     * SB2 (JDK8) 做法:
     *   String desc;
     *   switch (user.getStatus()) {
     *       case ACTIVE: desc = "活跃"; break;
     *       case SUSPENDED: desc = "暂停"; break;
     *       case DELETED: desc = "已删除"; break;
     *       default: desc = "未知";
     *   }
     *
     * JDK17 做法: switch 表达式, 无 break, 无 default (因为枚举已穷尽)
     */
    public static UserResponse fromEntity(User user) {
        // JDK17: switch 表达式直接返回值
        String statusDesc = switch (user.getStatus()) {
            case ACTIVE   -> "活跃";
            case SUSPENDED -> "暂停";
            case DELETED   -> "已删除";
            // 注意: 这里不需要 default, 因为 User.Status 是 enum 且所有值已列出
        };

        // JDK17: 方法引用 + Collectors.toSet()
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus().name(),
                statusDesc,
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
