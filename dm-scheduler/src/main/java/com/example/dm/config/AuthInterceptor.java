package com.example.dm.config;

import com.example.dm.util.AuthContext;
import com.example.dm.util.JwtUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        if (!requestUri.startsWith("/api/")) {
            return true;
        }

        if (isPublicApi(requestUri)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null) {
            writeJson(response, 401, "登录状态已失效，请重新登录");
            return false;
        }

        Integer userId = parseUserId(claims.get("userId"));
        String role = claims.get("role") == null ? null : String.valueOf(claims.get("role"));
        if (userId == null || role == null || role.trim().isEmpty()) {
            writeJson(response, 401, "登录凭证无效");
            return false;
        }

        AuthContext.set(userId, role);

        if (!isAuthorized(requestUri, method)) {
            writeJson(response, 403, "无权限访问该资源");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isPublicApi(String uri) {
        return "/api/user/login".equals(uri)
                || "/api/user/register".equals(uri)
                || "/health".equals(uri)
                || "/".equals(uri);
    }

    private boolean isAuthorized(String uri, String method) {
        if (uri.startsWith("/api/system/config/runtime") && HttpMethod.GET.matches(method)) {
            return AuthContext.hasAnyRole("ADMIN", "DM", "USER");
        }

        if (uri.startsWith("/api/statistics")
                || uri.startsWith("/api/store")
                || uri.startsWith("/api/system/config")
                || uri.startsWith("/api/operation-log")
                || uri.startsWith("/api/dm-schedule")) {
            return AuthContext.hasRole("ADMIN");
        }

        if (uri.startsWith("/api/user/list")
                || uri.startsWith("/api/user/page")
                || uri.startsWith("/api/user/add")
                || uri.startsWith("/api/user/update")
                || uri.startsWith("/api/user/delete")
                || uri.startsWith("/api/user/detail")) {
            return AuthContext.hasRole("ADMIN");
        }

        if (uri.startsWith("/api/dm/schedule")) {
            return AuthContext.hasAnyRole("ADMIN", "DM");
        }

        if (uri.startsWith("/api/dm")) {
            if (uri.startsWith("/api/dm/add") || uri.startsWith("/api/dm/update")
                    || uri.startsWith("/api/dm/delete") || uri.startsWith("/api/dm/page")) {
                return AuthContext.hasRole("ADMIN");
            }
            if (uri.startsWith("/api/dm/calculate-score")) {
                return AuthContext.hasAnyRole("ADMIN", "DM");
            }
            return AuthContext.hasAnyRole("ADMIN", "DM", "USER");
        }

        if (uri.startsWith("/api/script")) {
            if (uri.startsWith("/api/script/add") || uri.startsWith("/api/script/update") || uri.startsWith("/api/script/delete")) {
                return AuthContext.hasAnyRole("ADMIN", "DM");
            }
            return AuthContext.hasAnyRole("ADMIN", "DM", "USER");
        }

        if (uri.startsWith("/api/session")) {
            if (uri.startsWith("/api/session/auto-assign-next-week")) {
                return AuthContext.hasRole("ADMIN");
            }
            if (uri.startsWith("/api/session/add") || uri.startsWith("/api/session/update") || uri.startsWith("/api/session/delete")) {
                return AuthContext.hasAnyRole("ADMIN", "DM");
            }
            if (uri.startsWith("/api/session/start") || uri.startsWith("/api/session/complete")) {
                return AuthContext.hasAnyRole("ADMIN", "DM");
            }
            return AuthContext.hasAnyRole("ADMIN", "DM", "USER");
        }

        if (uri.startsWith("/api/order")) {
            if (uri.startsWith("/api/order/pay/")) {
                return AuthContext.hasAnyRole("ADMIN", "USER");
            }
            if (HttpMethod.GET.matches(method)) {
                return AuthContext.hasAnyRole("ADMIN", "USER");
            }
            return AuthContext.hasRole("ADMIN");
        }

        if (uri.startsWith("/api/reservation")
                || uri.startsWith("/api/evaluation")) {
            return AuthContext.hasAnyRole("ADMIN", "USER");
        }

        return true;
    }

    private Integer parseUserId(Object rawUserId) {
        if (rawUserId instanceof Integer) {
            return (Integer) rawUserId;
        }
        if (rawUserId instanceof Number) {
            return ((Number) rawUserId).intValue();
        }
        if (rawUserId instanceof String) {
            try {
                return Integer.valueOf((String) rawUserId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + escapeJson(message) + "\",\"data\":null}");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
