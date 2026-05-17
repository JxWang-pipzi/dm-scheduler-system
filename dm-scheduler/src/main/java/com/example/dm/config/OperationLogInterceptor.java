package com.example.dm.config;

import com.example.dm.entity.OperationLog;
import com.example.dm.mapper.OperationLogMapper;
import com.example.dm.util.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String action = mapAction(request.getMethod());
        if (action == null) {
            return;
        }

        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/")) {
            return;
        }
        if (uri.startsWith("/api/operation-log")) {
            return;
        }

        Integer userId = AuthContext.getUserId();
        if (userId == null) {
            return;
        }

        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setAction(action);
            log.setTarget(resolveTarget(uri));
            log.setDetail(action + " " + uri);
            log.setIp(resolveClientIp(request));
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志落库失败不影响主流程
        }
    }

    private String mapAction(String method) {
        if (HttpMethod.POST.matches(method)) {
            return "CREATE";
        }
        if (HttpMethod.PUT.matches(method)) {
            return "UPDATE";
        }
        if (HttpMethod.DELETE.matches(method)) {
            return "DELETE";
        }
        return null;
    }

    private String resolveTarget(String uri) {
        String normalized = uri.substring("/api/".length());
        int slashIndex = normalized.indexOf('/');
        String raw = slashIndex > 0 ? normalized.substring(0, slashIndex) : normalized;
        if (raw.isEmpty()) {
            return "SYSTEM";
        }
        return raw.toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            int index = xForwardedFor.indexOf(',');
            return (index >= 0 ? xForwardedFor.substring(0, index) : xForwardedFor).trim();
        }
        String ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.trim().isEmpty()) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
