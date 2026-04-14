package com.example.demo.repository;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository 层测试 — 使用 @DataJpaTest
 *
 * SB3 测试变化:
 * - @DataJpaTest 用法与 SB2 相同
 * - 自动配置 H2 内存数据库 (无需额外配置)
 * - 默认回滚事务 (每个测试方法后自动回滚)
 *
 * 演示: Text Block JPQL 查询的测试
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.save(new Role("ADMIN", "管理员"));
        Role userRole = roleRepository.save(new Role("USER", "普通用户"));

        testUser = new User("testuser", "test@example.com", "encoded_password");
        testUser.addRole(adminRole);
        testUser.addRole(userRole);
        userRepository.save(testUser);
    }

    @Test
    void findByUsername_existingUser() {
        var found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUsername_nonExisting() {
        var found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername() {
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void existsByEmail() {
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    /**
     * 测试 Text Block JPQL 搜索查询
     * 演示 JDK17 Text Block 在 @Query 中的应用
     */
    @Test
    void searchUsers_byUsername() {
        // JDK17: 使用 Text Block 注入的 JPQL 进行搜索
        Page<User> results = userRepository.searchUsers("test",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void searchUsers_byEmail() {
        Page<User> results = userRepository.searchUsers("example.com",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
    }

    @Test
    void searchUsers_noMatch() {
        Page<User> results = userRepository.searchUsers("nonexistent",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
    }
}
