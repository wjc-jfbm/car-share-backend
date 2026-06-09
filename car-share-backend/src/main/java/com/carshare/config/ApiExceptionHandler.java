package com.carshare.config;

import com.alibaba.fastjson2.JSON;
import com.carshare.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.carshare.controller")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            throw new RuntimeException(e);
        }

        log.error("API请求异常: {} - {}", uri, e.getMessage());

        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = "服务器内部错误";
        }

        if (msg.contains("Missing request attribute") || msg.contains("userId")) {
            return Result.fail("登录已过期，请重新登录");
        }

        return Result.fail(msg);
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public Result<?> handleMissingRequestValue(MissingRequestValueException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            throw new RuntimeException(e);
        }
        log.error("API请求参数缺失: {} - {}", uri, e.getMessage());
        return Result.fail("请求参数缺失");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            throw new RuntimeException(e);
        }
        log.error("API请求参数类型错误: {} - {}", uri, e.getMessage());
        return Result.fail("请求参数类型错误");
    }
}
