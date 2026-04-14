package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * JDK17 特性: Record 作为 DTO (数据传输对象)
 *
 * SB2 (JDK8) 做法:
 *   public class UserCreateRequest {
 *       @NotBlank private String username;
 *       // + getter, setter, equals, hashCode, toString ... (通常用 Lombok @Data)
 *   }
 *
 * JDK17 做法: record 一行搞定, 自动提供:
 *   - 全参构造器
 *   - getter (方法名就是字段名, 没有 get 前缀)
 *   - equals(), hashCode(), toString()
 *   - 不可变 (字段都是 final 的)
 *
 * 注意: record 可以直接使用 Bean Validation 注解 (@NotBlank, @Email 等)
 * SB3 中的 import 是 jakarta.validation.* (不是 javax.validation.*)
 */
public record UserCreateRequest(

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度 3-50 字符")
        String username,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度 8-100 字符")
        String password,

        /** 角色名称集合, 可以为 null (使用默认角色) */
        Set<String> roles

) {}
