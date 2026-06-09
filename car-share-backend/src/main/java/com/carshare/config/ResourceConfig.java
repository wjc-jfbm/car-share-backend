package com.carshare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.access-path:/uploads}")
    private String accessPath;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(accessPath + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/phone-login",
                        "/api/user/wx-phone-login",
                        "/api/user/register",
                        "/api/file/**",
                        "/api/health",
                        "/api/statistics/platform",
                        "/api/car/list",
                        "/api/car/detail",
                        "/api/goods/list",
                        "/api/goods/all",
                        "/api/goods/detail"
                );
    }
}
