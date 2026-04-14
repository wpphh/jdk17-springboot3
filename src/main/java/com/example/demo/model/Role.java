package com.example.demo.model;

// SB3: 全部使用 jakarta.* 包名
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 角色实体
 *
 * 简单的 JPA 实体, 演示基本的 @Entity 用法.
 * 与 SB2 的区别: import 包名从 javax.persistence.* 改为 jakarta.persistence.*
 */
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    // JPA 需要无参构造器 (SB3 仍然如此, record 不可用于 @Entity)
    public Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ========== Getter / Setter ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Role{id=%d, name='%s'}".formatted(getId(), name);
    }
}
