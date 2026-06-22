package com.carshare.config;

import com.carshare.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLSyntaxErrorException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.carshare.controller")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** 通用异常兜底（仅处理 /api/ 路径，由 basePackages 限定） */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.error("API异常 [{}]: {}", uri, e.getMessage());

        String msg = e.getMessage();

        // 认证过期
        if (msg != null && (msg.contains("Missing request attribute") || msg.contains("userId"))) {
            return Result.unauthorized("登录已过期，请重新登录");
        }

        return Result.fail("服务器内部错误");
    }

    /** 数据库异常（表不存在、SQL语法错误等） */
    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccess(DataAccessException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.error("数据库异常 [{}]: {}", uri, e.getMessage());

        Throwable cause = e.getCause();
        if (cause instanceof SQLSyntaxErrorException) {
            String sqlMsg = cause.getMessage();
            if (sqlMsg != null && sqlMsg.contains("doesn't exist")) {
                // 提取缺失的表名
                String tableName = sqlMsg.replaceAll(".*Table '(.*?)'.*", "$1");
                log.warn("缺失数据库表: {}", tableName);
                return Result.fail("系统配置异常，请联系管理员");
            }
            return Result.fail("数据库语法错误");
        }
        return Result.fail("数据操作异常");
    }

    /** 请求参数缺失 */
    @ExceptionHandler(MissingRequestValueException.class)
    public Result<?> handleMissingRequestValue(MissingRequestValueException e, HttpServletRequest request) {
        log.warn("请求参数缺失 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.fail("请求参数缺失");
    }

    /** 参数类型错误 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型错误 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.fail("请求参数类型错误");
    }

    /** 请求体格式错误（如JSON解析失败） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.fail("请求数据格式错误");
    }
}
