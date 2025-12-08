package com.ShopSphere.shop_sphere.security;

import jakarta.servlet.http.HttpServletRequest;

public class SecurityUtil {

    public static int getLoggedInUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User not authenticated");
        }
        return (int) userIdObj;
    }

    public static String getLoggedInUserRole(HttpServletRequest request) {
        Object roleObj = request.getAttribute("role");
        if (roleObj == null) {
            throw new RuntimeException("User role missing");
        }
        return (String) roleObj;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        String role = getLoggedInUserRole(request);
        return role.equalsIgnoreCase("ADMIN");
    }
}
