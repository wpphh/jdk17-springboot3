package com.example.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体 — 演示 JPA 关联关系
 *
 * 注意: 这里使用 enum AccountStatus (用于 JPA @Enumerated), 而不是 sealed interface.
 * 原因: JPA 的 @Enumerated 只支持 enum, 不支持 sealed interface.
 * sealed interface 版本的 AccountStatus 在 model/types/ 中, 用于业务逻辑层的类型安全.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    // SB3: @Enumerated 用法不变, 但 import 是 jakarta.persistence.EnumType
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    /**
     * 多对多关系 — 用户与角色
     * SB3: 用法与 SB2 相同, 只是 import 包名变了
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * 用户状态枚举 — 用于 JPA 持久化
     * (sealed interface 版本在 model/types/AccountStatus.java, 用于业务逻辑)
     */
    public enum Status {
        ACTIVE, SUSPENDED, DELETED
    }

    // ========== 构造器 ==========

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // ========== 辅助方法 ==========

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    // ========== Getter / Setter ==========

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        // JDK17: String.formatted() 替代 String.format()
        return "User{id=%d, username='%s', email='%s', status=%s}".formatted(
                getId(), username, email, status);
    }
}
