package com.carshare.config;

import com.alibaba.fastjson2.JSON;
import com.carshare.common.Result;
import com.carshare.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (JwtUtil.validateToken(token)) {
                    Long userId = JwtUtil.getUserIdFromToken(token);
                    request.setAttribute("userId", userId);
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/user/login") ||
            requestURI.startsWith("/api/user/phone-login") ||
            requestURI.startsWith("/api/user/wx-phone-login") ||
            requestURI.startsWith("/api/user/register") ||
            requestURI.startsWith("/api/file/") ||
            requestURI.startsWith("/api/health") ||
            requestURI.startsWith("/api/statistics/platform") ||
            requestURI.startsWith("/api/car/list") ||
            requestURI.startsWith("/api/car/detail") ||
            requestURI.startsWith("/api/goods/list") ||
            requestURI.startsWith("/api/goods/all") ||
            requestURI.startsWith("/api/goods/detail")) {
            return true;
        }

        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.unauthorized("登录已过期，请重新登录")));
        response.getWriter().flush();
        return false;
    }
}
