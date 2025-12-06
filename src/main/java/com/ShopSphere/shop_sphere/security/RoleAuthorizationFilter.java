package com.ShopSphere.shop_sphere.security;


import java.util.Arrays;
 
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
 
import com.ShopSphere.shop_sphere.exception.ForbiddenException;
import com.ShopSphere.shop_sphere.exception.UnauthorizedException;
 
@Component
public class RoleAuthorizationFilter implements HandlerInterceptor {
 
    private final JwtTokenUtil jwtTokenUtil;
 
    public RoleAuthorizationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }
 
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
 
        // Skip non-controller methods (e.g., static resources)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
 
        HandlerMethod method = (HandlerMethod) handler;
        AllowedRoles allowedRoles = method.getMethodAnnotation(AllowedRoles.class);
 
        // If no @AllowedRoles, no restriction
        if (allowedRoles == null) {
            return true;
        }
 
        // Get JWT token from header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
 
        String token = authHeader.substring(7); // remove "Bearer "
        String role;
 
        try {
            role = jwtTokenUtil.extractRole(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
 
        // Check if role matches allowed roles
        boolean allowed = Arrays.stream(allowedRoles.value())
                                .anyMatch(r -> r.equalsIgnoreCase(role));
 
        if (!allowed) {
            throw new ForbiddenException("You do not have permission to access this resource");
        }
 
        return true; // allowed, continue
    }
}
 
 
//import com.ShopSphere.shop_sphere.exception.ForbiddenException;
//import com.ShopSphere.shop_sphere.exception.UnauthorizedException;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.util.Arrays;
 
//@Component
//public class RoleAuthorizationFilter implements Filter {
// 
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
//            throws IOException, ServletException {
// 
//        HttpServletRequest request = (HttpServletRequest) req;
// 
//        String role = (String) request.getAttribute("role");
// 
//        // No JWT? Skip
//        if (role == null) {
//            chain.doFilter(req, res);
//            return;
//        }
// 
//        // Check @AllowedRoles annotation on controller method
//        HandlerMethod method = (HandlerMethod) request.getAttribute("handlerMethod");
// 
//        if (method != null) {
//            AllowedRoles annotation = method.getMethod().getAnnotation(AllowedRoles.class);
//            if (annotation != null) {
//                boolean allowed = false;
//                for (String r : annotation.value()) {
//                    if (r.equalsIgnoreCase(role)) {
//                        allowed = true;
//                        break;
//                    }
//                }
//                if (!allowed) {
//                    throw new ForbiddenException("Access denied: Role not allowed");
//                }
//            }
//        }
// 
//        chain.doFilter(req, res);
//    }
//}
// 

//@Component
//public class RoleAuthorizationFilter implements HandlerInterceptor {
// 
//    private final JwtService jwtService;
// 
//    public RoleAuthorizationFilter(JwtService jwtService) {
//        this.jwtService = jwtService;
//    }
// 
//    @Override
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response, Object handler) throws Exception {
// 
//        if (!(handler instanceof HandlerMethod)) {
//            return true; // skip non-method endpoints
//        }
// 
//        HandlerMethod method = (HandlerMethod) handler;
//        AllowedRoles allowedRoles = method.getMethodAnnotation(AllowedRoles.class);
// 
//        if (allowedRoles == null) {
//            return true; // no role restriction
//        }
// 
//        // Get JWT token from header
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new UnauthorizedException("Missing or invalid Authorization header");
//        }
// 
//        String token = authHeader.substring(7);
//        String role = jwtService.extractRole(token); // extract role
// 
//        // Normalize role
////        role = role.toUpperCase();
// 
//        boolean allowed = Arrays.stream(allowedRoles.value())
//                                .anyMatch(r -> r.equalsIgnoreCase(role.toUpperCase()));
// 
//        if (!allowed) {
//            throw new ForbiddenException("You do not have permission to access this resource");
//        }
// 
//        return true;
//    }
//}
// 
