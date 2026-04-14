package com.example.demo.util;

import com.example.demo.model.User;
import com.example.demo.model.types.AccountStatus;
import com.example.demo.model.types.ApiResponse;

import java.time.Duration;
import java.util.stream.Collectors;

/**
 * JDK17 语法特性集中演示
 *
 * 本文件将 JDK8 → JDK17 的关键语法变化集中在一个地方, 每个方法都附带"SB2 (JDK8) 做法"对比.
 * 建议阅读顺序: 1→2→3→4→5
 */
public class PatternMatchingDemo {

    // ============================================================
    // 1. instanceof Pattern Matching (JDK16 正式引入)
    // ============================================================

    /**
     * SB2 (JDK8) 做法:
     *   if (obj instanceof User) {
     *       User user = (User) obj;            // 需要显式强转
     *       return user.getUsername();
     *   }
     *
     * JDK17 做法: instanceof 后直接绑定变量, 无需强转
     */
    public static String extractUserName(Object obj) {
        // JDK17: if (obj instanceof User user) — 类型匹配的同时自动绑定到局部变量 user
        if (obj instanceof User user) {
            return user.getUsername();
        }
        return "未知对象";
    }

    /**
     * 进阶: instanceof + 条件判断 (JDK16 支持)
     * SB2 做法: if (obj instanceof User) { User u = (User) obj; if (u.getUsername().length() > 3) ... }
     * JDK17:    if (obj instanceof User u && u.getUsername().length() > 3) — 一步到位
     */
    public static boolean isLongUsername(Object obj) {
        // && 后面可以直接使用绑定的变量 u
        return obj instanceof User u && u.getUsername().length() > 3;
    }

    // ============================================================
    // 2. Switch 表达式 (JDK14 正式引入)
    // ============================================================

    /**
     * JDK17 注意: switch 表达式可在 enum 上直接使用, 但 sealed type 上的 pattern matching
     * switch 需要 JDK21. 所以这里用 if/else + instanceof pattern matching (JDK16 标准).
     */
    public static String describeAccountStatus(AccountStatus status) {
        if (status instanceof AccountStatus.Active) {
            return "活跃用户";
        } else if (status instanceof AccountStatus.Suspended s) {
            // JDK17: instanceof pattern matching — 匹配时绑定变量 s
            return "已暂停: " + s.reason();
        } else if (status instanceof AccountStatus.Deleted) {
            return "已删除";
        }
        throw new IllegalArgumentException("未知状态");
    }

    // ============================================================
    // 3. Text Blocks 文本块 (JDK15 正式引入)
    // ============================================================

    /**
     * SB2 (JDK8) 做法:
     *   String sql = "SELECT u.id, u.username, u.email " +
     *                "FROM users u " +
     *                "WHERE u.status = ? " +
     *                "ORDER BY u.created_at DESC";
     *
     * JDK17 做法: 三引号 (""") 包裹多行文本, 自动处理缩进, 无需拼接
     */
    public static String buildUserQuery(String status) {
        // Text block: 自动去除公共前导空白, 保留相对缩进
        return """
                SELECT u.id, u.username, u.email
                FROM users u
                WHERE u.status = '%s'
                ORDER BY u.created_at DESC
                """.formatted(status);
    }

    /**
     * Text block 的另一个用途: 生成 JSON 模板
     * SB2 做法: 用转义符拼接 JSON 字符串, 极难阅读
     */
    public static String buildJsonTemplate() {
        return """
                {
                    "username": "%s",
                    "email": "%s",
                    "status": "%s"
                }
                """;
    }

    // ============================================================
    // 4. Sealed Interface + Pattern Matching 联合使用
    // ============================================================

    /**
     * 处理 ApiResponse — 展示 sealed + instanceof pattern matching
     *
     * SB2 (JDK8) 做法:
     *   if (result.isSuccess()) {
     *       User data = result.getData();
     *   } else {
     *       String msg = result.getMessage();
     *   }
     *
     * JDK17 做法: instanceof pattern matching 自动区分类型, 变量类型安全
     * (JDK21 中可以用 switch 表达式直接匹配 sealed type)
     */
    public static String formatResponse(ApiResponse<?> response) {
        if (response instanceof ApiResponse.Success<?> s) {
            // JDK17: instanceof 匹配 sealed 子类型, 自动绑定为局部变量 s
            return "成功 [%s]: %s".formatted(s.message(), s.data());
        } else if (response instanceof ApiResponse.Error e) {
            return "错误 [%s] %s: %s".formatted(e.code(), e.message(),
                    e.details().isEmpty() ? "无详情" : e.details());
        }
        throw new IllegalArgumentException("未知响应类型");
    }

    // ============================================================
    // 5. Switch 表达式 + 枚举 (无需 sealed 也很好用)
    // ============================================================

    /**
     * Duration 格式化 — 使用传统 if-else
     *
     * JDK17: switch 表达式在 enum 上很好用, 但带条件的 pattern matching (guarded patterns)
     * 如 "case int s when s < 60" 是 JDK21 特性, JDK17 中用 if-else 实现
     */
    public static String formatDuration(Duration duration) {
        if (duration == null) return "未知";
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分" + (seconds % 60) + "秒";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "时" + ((seconds % 3600) / 60) + "分";
        } else {
            return (seconds / 86400) + "天";
        }
    }

    // ============================================================
    // 6. Stream 和 Optional 的改进 (JDK9-JDK16 逐步增强)
    // ============================================================

    /**
     * Stream.toList() (JDK16) — 替代 .collect(Collectors.toList())
     *
     * SB2 (JDK8) 做法: list.stream().filter(...).collect(Collectors.toList())
     * JDK17 做法:      list.stream().filter(...).toList()  — 返回不可变 List
     */
    public static java.util.List<String> filterNonEmpty(java.util.List<String> items) {
        return items.stream()
                .filter(s -> s != null && !s.isBlank())  // JDK11: isBlank()
                .toList();                                 // JDK16: toList() 返回不可变 List
    }

    /**
     * Optional.stream() (JDK9) — 将 Optional 转为 Stream, 简化 flatMap 操作
     *
     * SB2 (JDK8) 做法: 需要 if (opt.isPresent()) list.add(opt.get())
     * JDK17 做法:      opt.stream().toList()
     */
    public static <T> java.util.List<T> collectPresent(java.util.Optional<T> opt) {
        return opt.stream().toList();
    }
}
