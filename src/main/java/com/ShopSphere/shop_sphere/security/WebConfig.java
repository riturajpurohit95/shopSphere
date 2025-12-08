package com.ShopSphere.shop_sphere.security;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
@Configuration
public class WebConfig implements WebMvcConfigurer {
 
    private final RoleAuthorizationFilter roleAuthorizationFilter;
 
    public WebConfig(RoleAuthorizationFilter roleAuthorizationFilter) {
        this.roleAuthorizationFilter = roleAuthorizationFilter;
    }
 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleAuthorizationFilter)
                .addPathPatterns("/api/**"); // intercept all API paths
    }
}
 
