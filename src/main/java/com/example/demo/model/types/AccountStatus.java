package com.example.demo.model.types;

import java.time.Instant;

/**
 * JDK17 特性: Sealed Interface (密封接口)
 *
 * sealed 修饰符限制了哪些类可以实现此接口, 编译器可以据此进行穷尽性检查.
 * SB2 (JDK8) 做法: 用 enum 或普通 interface, 无法限制实现类, 也无法在 switch 中穷尽匹配.
 *
 * 关键字说明:
 * - sealed: 声明密封接口/类
 * - permits: 指定允许实现的子类 (必须在同一模块或同一包)
 * - record: 不可变数据类 (JDK16 正式引入), 自带 constructor/getter/equals/hashCode/toString
 */
public sealed interface AccountStatus
        permits AccountStatus.Active, AccountStatus.Suspended, AccountStatus.Deleted {

    /**
     * 活跃状态 - record 实现 sealed interface
     * record 自动提供: 构造器、getter (方法名即 getter, 无 get 前缀)、equals、hashCode、toString
     */
    record Active(Instant since) implements AccountStatus {}

    /**
     * 暂停状态 - record 可以有多个字段
     */
    record Suspended(Instant since, String reason) implements AccountStatus {}

    /**
     * 删除状态
     */
    record Deleted(Instant at) implements AccountStatus {}

    // ========== 工具方法 ==========

    /**
     * 获取状态描述
     *
     * JDK17 注意: switch 表达式可以在 enum 上直接使用, 但不能直接在 sealed type 上
     * 做 pattern matching switch (那是 JDK21 的特性).
     * 所以这里使用 if/else if + instanceof pattern matching (JDK16 标准特性).
     *
     * SB2 (JDK8) 做法:
     *   if (status instanceof AccountStatus.Active) {
     *       AccountStatus.Active a = (AccountStatus.Active) status;
     *       return "活跃用户";
     *   }
     *
     * JDK17 做法: instanceof 后自动绑定变量, 无需强转
     */
    static String describe(AccountStatus status) {
        // JDK17: instanceof pattern matching — 匹配的同时绑定到局部变量
        if (status instanceof Active a) {
            return "活跃用户 (自 " + a.since() + ")";
        } else if (status instanceof Suspended s) {
            return "已暂停 (原因: " + s.reason() + ", 自 " + s.since() + ")";
        } else if (status instanceof Deleted d) {
            return "已删除 (于 " + d.at() + ")";
        }
        // sealed interface 保证了只有这三种实现, 理论上不会走到这里
        throw new IllegalArgumentException("未知状态");
    }
}
