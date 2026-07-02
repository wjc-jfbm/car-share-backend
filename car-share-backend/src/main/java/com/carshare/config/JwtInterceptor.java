package com.carshare.config;

import com.alibaba.fastjson2.JSON;
import com.carshare.common.Result;
import com.carshare.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    request.setAttribute("userId", userId);
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        String requestURI = request.getRequestURI();
        for (String path : JwtExcludedPaths.EXCLUDED_PATHS) {
            if (requestURI.equals(path) || requestURI.startsWith(path + "/")) {
                return true;
            }
        }

        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.unauthorized("登录已过期，请重新登录")));
        response.getWriter().flush();
        return false;
    }
}
