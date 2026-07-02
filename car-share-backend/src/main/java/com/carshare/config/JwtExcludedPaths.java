package com.carshare.config;

import java.util.Set;

/**
 * JWT 认证豁免路径集中管理
 * 在此处添加路径后，JwtInterceptor 和 ResourceConfig 都会自动生效
 */
public final class JwtExcludedPaths {

    /** 无需 JWT 认证的公开 API 路径前缀 */
    public static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/user/login",
            "/api/user/phone-login",
            "/api/user/wx-phone-login",
            "/api/user/register",
            "/api/file",
            "/api/health",
            "/api/statistics/platform",
            "/api/car/list",
            "/api/car/detail",
            "/api/goods/list",
            "/api/goods/all",
            "/api/goods/detail"
    );

    private JwtExcludedPaths() {}
}
