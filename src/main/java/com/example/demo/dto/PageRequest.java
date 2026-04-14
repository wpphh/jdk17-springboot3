package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 分页请求参数 — Record DTO
 *
 * JDK17: record 作为简单的参数载体, 比 SB2 的手写类简洁得多
 */
public record PageRequest(

        @Min(value = 0, message = "页码不能小于 0")
        int page,

        @Min(value = 1, message = "每页数量不能小于 1")
        int size,

        /** 排序字段 */
        String sortBy,

        /** 排序方向: asc 或 desc */
        @Pattern(regexp = "^(asc|desc)$", message = "排序方向只能是 asc 或 desc")
        String sortDir

) {
    /** 默认构造: 第 0 页, 每页 10 条, 按 id 降序 */
    public PageRequest() {
        this(0, 10, "id", "desc");
    }

    /** 转换为 Spring Data 的 Pageable */
    public org.springframework.data.domain.Pageable toPageable() {
        var direction = "asc".equalsIgnoreCase(sortDir)
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(page, size, direction, sortBy);
    }
}
