package com.example.dm.util;

import java.util.Locale;

/**
 * 当前请求认证上下文（基于 ThreadLocal）。
 */
public final class AuthContext {
    private static final ThreadLocal<Integer> USER_ID = new ThreadLocal<Integer>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<String>();

    private AuthContext() {
    }

    public static void set(Integer userId, String role) {
        USER_ID.set(userId);
        ROLE.set(role == null ? null : role.trim().toUpperCase(Locale.ROOT));
    }

    public static Integer getUserId() {
        return USER_ID.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static boolean hasRole(String role) {
        String currentRole = getRole();
        return currentRole != null && currentRole.equalsIgnoreCase(role);
    }

    public static boolean hasAnyRole(String... roles) {
        String currentRole = getRole();
        if (currentRole == null || roles == null) {
            return false;
        }
        for (String role : roles) {
            if (role != null && currentRole.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isDm() {
        return hasRole("DM");
    }

    public static boolean isUser() {
        return hasRole("USER");
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
