package com.example.demo.model.types;

import java.util.Map;

/**
 * JDK17 特性: Sealed Interface + Record 实现代数数据类型 (Algebraic Data Type)
 *
 * SB2 (JDK8) 做法: 通用的 Result<T> 类, 包含 code/message/data 字段, 用 null 判断成功/失败
 * JDK17 做法: sealed interface 限制只有 Success 和 Error 两种情况,
 *            调用方通过 pattern matching switch 自动区分, 无需 null 检查
 *
 * 使用示例:
 *   ApiResponse<UserResponse> result = userService.getUserById(1L);
 *   switch (result) {
 *       case ApiResponse.Success<UserResponse> s -> System.out.println("成功: " + s.data());
 *       case ApiResponse.Error e -> System.out.println("失败: " + e.message());
 *   }
 */
public sealed interface ApiResponse<T> permits ApiResponse.Success, ApiResponse.Error {

    /**
     * 成功响应 - 携带数据和可选消息
     */
    record Success<T>(T data, String message) implements ApiResponse<T> {

        /** 便捷工厂方法: 仅返回数据, 无消息 */
        public static <T> Success<T> of(T data) {
            return new Success<>(data, "操作成功");
        }
    }

    /**
     * 错误响应 - 携带错误码、消息和可选详细信息
     *
     * 注意: Error 不是泛型, 因为错误响应不携带业务数据
     */
    record Error(String code, String message, Map<String, Object> details) implements ApiResponse<Object> {

        /** 便捷工厂方法: 仅错误码和消息 */
        public static Error of(String code, String message) {
            return new Error(code, message, Map.of());
        }
    }

    // ========== 便捷方法 ==========

    /** 创建成功响应 */
    @SuppressWarnings("unchecked")
    static <T> ApiResponse<T> success(T data, String message) {
        return (ApiResponse<T>) new Success<>(data, message);
    }

    /** 创建成功响应 (无消息) */
    @SuppressWarnings("unchecked")
    static <T> ApiResponse<T> success(T data) {
        return (ApiResponse<T>) Success.of(data);
    }

    /** 创建错误响应 */
    @SuppressWarnings("unchecked")
    static <T> ApiResponse<T> error(String code, String message) {
        return (ApiResponse<T>) Error.of(code, message);
    }
}
