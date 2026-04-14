package com.example.demo.service;

import com.example.demo.dto.PageRequest;
import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.types.ApiResponse;
import org.springframework.data.domain.Page;

/**
 * 用户服务接口
 *
 * 返回类型使用 ApiResponse (sealed interface) — 展示 sealed interface 在业务层的应用
 */
public interface UserService {

    /** 获取用户列表 (分页) */
    ApiResponse<Page<UserResponse>> getUserList(PageRequest pageRequest);

    /** 根据 ID 获取用户 */
    ApiResponse<UserResponse> getUserById(Long id);

    /** 创建用户 */
    ApiResponse<UserResponse> createUser(UserCreateRequest request);

    /** 更新用户状态 */
    ApiResponse<UserResponse> updateUserStatus(Long id, String status);

    /** 删除用户 */
    ApiResponse<Void> deleteUser(Long id);

    /** 搜索用户 */
    ApiResponse<Page<UserResponse>> searchUsers(String keyword, PageRequest pageRequest);
}
