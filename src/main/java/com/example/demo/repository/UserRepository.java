package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户 Repository
 *
 * JDK17 特性演示: Text Block (文本块) 用于 JPQL 查询
 *
 * SB2 (JDK8) 做法:
 *   @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
 *          "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY u.createdAt DESC")
 *
 * JDK17 做法: 使用 Text Block (三引号), 多行 SQL 更清晰, 无需拼接
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据用户名查找 */
    Optional<User> findByUsername(String username);

    /** 检查用户名是否存在 */
    boolean existsByUsername(String username);

    /** 检查邮箱是否存在 */
    boolean existsByEmail(String email);

    /**
     * 搜索用户 — 使用 Text Block 写 JPQL
     *
     * JDK17 Text Block 优势:
     * 1. 多行字符串无需拼接 (+)
     * 2. 自动去除公共缩进
     * 3. SQL 语法高亮在 IDE 中更友好
     */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /** 根据状态查询用户 (分页) */
    Page<User> findByStatus(User.Status status, Pageable pageable);
}
