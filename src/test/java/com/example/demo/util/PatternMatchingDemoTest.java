package com.example.demo.util;

import com.example.demo.model.User;
import com.example.demo.model.types.AccountStatus;
import com.example.demo.model.types.ApiResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JDK17 特性单元测试
 *
 * 纯 Java 测试, 不需要 Spring 上下文.
 * 测试 switch 表达式、pattern matching、sealed interface、text block 等特性.
 */
class PatternMatchingDemoTest {

    // ========== instanceof Pattern Matching ==========

    @Test
    void extractUserName_fromUser_returnsUsername() {
        User user = new User("testuser", "test@example.com", "pass123");

        // JDK17: instanceof pattern matching 自动绑定变量
        String result = PatternMatchingDemo.extractUserName(user);
        assertThat(result).isEqualTo("testuser");
    }

    @Test
    void extractUserName_fromNonUser_returnsUnknown() {
        String result = PatternMatchingDemo.extractUserName("not a user");
        assertThat(result).isEqualTo("未知对象");
    }

    @Test
    void isLongUsername_withLongName_returnsTrue() {
        User user = new User("admin", "admin@example.com", "pass123");
        // "admin" 长度 5 > 3
        assertThat(PatternMatchingDemo.isLongUsername(user)).isTrue();
    }

    @Test
    void isLongUsername_withShortName_returnsFalse() {
        User user = new User("ab", "ab@example.com", "pass123");
        assertThat(PatternMatchingDemo.isLongUsername(user)).isFalse();
    }

    // ========== Switch Expression + Sealed Interface ==========

    @Test
    void describeAccountStatus_active() {
        AccountStatus.Active active = new AccountStatus.Active(Instant.now());
        String desc = PatternMatchingDemo.describeAccountStatus(active);
        assertThat(desc).isEqualTo("活跃用户");
    }

    @Test
    void describeAccountStatus_suspended() {
        AccountStatus.Suspended suspended = new AccountStatus.Suspended(
                Instant.now(), "违规操作");
        String desc = PatternMatchingDemo.describeAccountStatus(suspended);
        assertThat(desc).isEqualTo("已暂停: 违规操作");
    }

    @Test
    void describeAccountStatus_deleted() {
        AccountStatus.Deleted deleted = new AccountStatus.Deleted(Instant.now());
        String desc = PatternMatchingDemo.describeAccountStatus(deleted);
        assertThat(desc).isEqualTo("已删除");
    }

    // ========== Text Block ==========

    @Test
    void buildUserQuery_returnsFormattedSql() {
        String sql = PatternMatchingDemo.buildUserQuery("ACTIVE");
        assertThat(sql).contains("SELECT u.id, u.username, u.email");
        assertThat(sql).contains("WHERE u.status = 'ACTIVE'");
        assertThat(sql).contains("ORDER BY u.created_at DESC");
    }

    // ========== Sealed Interface + Pattern Matching ==========

    @Test
    void formatResponse_success() {
        ApiResponse<String> response = ApiResponse.success("data", "done");
        String formatted = PatternMatchingDemo.formatResponse(response);
        assertThat(formatted).contains("成功");
        assertThat(formatted).contains("done");
        assertThat(formatted).contains("data");
    }

    @Test
    void formatResponse_error() {
        ApiResponse<Object> response = ApiResponse.error("ERR_001", "出错了");
        String formatted = PatternMatchingDemo.formatResponse(response);
        assertThat(formatted).contains("错误");
        assertThat(formatted).contains("ERR_001");
        assertThat(formatted).contains("出错了");
    }

    // ========== Duration Formatting ==========

    @Test
    void formatDuration_seconds() {
        assertThat(PatternMatchingDemo.formatDuration(Duration.ofSeconds(30)))
                .isEqualTo("30秒");
    }

    @Test
    void formatDuration_minutes() {
        assertThat(PatternMatchingDemo.formatDuration(Duration.ofMinutes(5)))
                .isEqualTo("5分0秒");
    }

    @Test
    void formatDuration_null() {
        assertThat(PatternMatchingDemo.formatDuration(null)).isEqualTo("未知");
    }

    // ========== Stream.toList() and Optional.stream() ==========

    @Test
    void filterNonEmpty_removesBlanks() {
        List<String> input = List.of("hello", "", null, "world", "  ");
        List<String> result = PatternMatchingDemo.filterNonEmpty(input);
        assertThat(result).containsExactly("hello", "world");
    }

    @Test
    void collectPresent_withValue() {
        Optional<String> opt = Optional.of("found");
        List<String> result = PatternMatchingDemo.collectPresent(opt);
        assertThat(result).containsExactly("found");
    }

    @Test
    void collectPresent_empty() {
        List<String> result = PatternMatchingDemo.collectPresent(Optional.empty());
        assertThat(result).isEmpty();
    }
}
