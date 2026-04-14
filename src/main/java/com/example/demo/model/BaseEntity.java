package com.example.demo.model;

// SB3 重大变化: javax.* → jakarta.*
// SB2: import javax.persistence.*;
// SB3: import jakarta.persistence.*;
// 原因: Java EE 被移交给 Eclipse 基金会后更名为 Jakarta EE, 包名全部从 javax 改为 jakarta
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * 实体基类 — 所有 JPA 实体继承此类
 *
 * 注意: JPA 实体必须用 class (不能用 record), 因为:
 * 1. record 是 final 的, JPA 需要代理类 (Hibernate 通过继承生成代理)
 * 2. record 没有无参构造器, JPA 需要无参构造器来反射创建实例
 * 3. record 的字段是 final 的, JPA 需要通过 setter 修改字段
 *
 * 所以: model/ 用 class (JPA 实体), dto/ 用 record (不可变数据传输对象)
 */
@MappedSuperclass
public abstract class BaseEntity {

    // SB3: 仍然使用 @Id 和 @GeneratedValue, 但 import 是 jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== Getter / Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
