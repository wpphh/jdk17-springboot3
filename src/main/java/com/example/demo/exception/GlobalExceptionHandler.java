package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 — 使用 ProblemDetail (SB3 重大新特性!)
 *
 * ===================== SB2 (JDK8) 做法 =====================
 *
 *   @ExceptionHandler(ResourceNotFoundException.class)
 *   public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
 *       ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage());
 *       return ResponseEntity.status(404).body(error);
 *   }
 *   // 需要自己定义 ErrorResponse 类, 各项目的格式不统一
 *
 * ===================== SB3 做法 =====================
 *
 * 使用 ProblemDetail (RFC 7807 标准), Spring 6 内置支持:
 * - 不需要自己定义错误响应类
 * - 符合 RFC 7807 标准, 各项目格式统一
 * - 支持 type/title/status/detail/properties 扩展
 *
 * 返回示例:
 * {
 *   "type": "about:blank",
 *   "title": "Resource Not Found",
 *   "status": 404,
 *   "detail": "用户不存在: ID=123"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理资源未找到异常 → 404 ProblemDetail
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        // SB3: ProblemDetail.forStatusAndDetail() 创建符合 RFC 7807 的错误响应
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }

    /**
     * 处理业务异常 → 400 ProblemDetail (携带错误码)
     */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Business Error");
        // SB3: 通过 properties 添加自定义字段 (如错误码)
        problem.setProperty("code", ex.getCode());
        return problem;
    }

    /**
     * 处理参数校验异常 → 400 ProblemDetail (携带字段错误详情)
     *
     * SB2 做法: 手动收集 FieldError, 放入自定义响应类
     * SB3 做法: 使用 ProblemDetail.properties 扩展字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // JDK17: 使用 Map.of 或 HashMap 收集字段错误
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "参数校验失败");
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    /**
     * 处理其他未捕获的异常 → 500 ProblemDetail
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
        problem.setTitle("Internal Server Error");
        // 生产环境不应暴露异常详情, 这里仅为演示
        return problem;
    }
}
