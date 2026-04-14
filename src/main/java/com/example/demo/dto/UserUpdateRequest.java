package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 用户更新请求 — Record DTO
 *
 * 更新请求的字段都是可选的 (用 null 表示不修改), 区分:
 * - 字段为 null: 不修改该字段
 * - 字段有值: 更新为新值
 *
 * 注意: 不要用 Optional 作为 record 的字段! Jackson 无法直接反序列化 Optional.
 * 这里用 null 表示"不修改", 在 Service 层通过 null 检查处理.
 *
 * JDK17: record 作为 DTO, 不可变, 简洁
 */
public record UserUpdateRequest(

        @Size(min = 3, max = 50, message = "用户名长度 3-50 字符")
        String username,

        @Email(message = "邮箱格式不正确")
        String email,

        @Size(min = 8, max = 100, message = "密码长度 8-100 字符")
        String password

) {}
