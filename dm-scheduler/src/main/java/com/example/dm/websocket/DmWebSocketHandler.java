package com.example.dm.websocket;

import com.example.dm.entity.Notification;
import com.example.dm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DmWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private NotificationService notificationService;

    private static class UserSession {
        final WebSocketSession session;
        final Integer userId;
        final String role;

        UserSession(WebSocketSession session, Integer userId, String role) {
            this.session = session;
            this.userId = userId;
            this.role = role;
        }
    }

    // 以 userId 为 key 存储所有在线用户的 WebSocket 会话
    private static final Map<Integer, UserSession> onlineSessions = new ConcurrentHashMap<>();

    /* ==================== 生命周期回调 ==================== */

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        Integer userId = extractParam(uri, "userId");
        String role = extractStringParam(uri, "role");
        if (userId != null) {
            onlineSessions.put(userId, new UserSession(session, userId, role != null ? role.toUpperCase() : "UNKNOWN"));
            System.out.println(
                    "[WebSocket] 用户上线 -> userId=" + userId + ", role=" + role + ", 当前在线: " + onlineSessions.size());
            // 回复连接成功
            session.sendMessage(
                    new TextMessage("{\"type\":\"connect\",\"message\":\"WebSocket 连接成功\",\"userId\":" + userId + "}"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("[WebSocket] 收到消息: " + payload);
        String type = extractJsonField(payload, "type");
        System.out.println("[WebSocket] 解析type: " + type);
        if ("PING".equals(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
        } else if ("CALL_SERVICE".equals(type)) {
            handleCallService(session, payload);
        }
    }

    private void handleCallService(WebSocketSession senderSession, String payload) throws Exception {
        String content = extractJsonField(payload, "content");
        String room = extractJsonField(payload, "room");
        String userIdStr = extractJsonFieldOrNumber(payload, "userId");
        String role = extractJsonField(payload, "role");

        String displayContent = (room != null && !room.isEmpty() ? "包厢: " + room + " | " : "") + (content != null ? content : "");

        Notification notification = new Notification();
        notification.setType("CALL_SERVICE");
        notification.setTitle("DM呼叫前台");
        notification.setContent(displayContent);
        notification.setIsRead(0);
        notification.setUserId(null);
        try {
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            System.err.println("[WebSocket] 通知写入数据库失败: " + e.getMessage());
        }

        String notificationJson = String.format(
                "{\"type\":\"CALL_SERVICE\",\"message\":\"DM呼叫前台\",\"content\":\"%s\",\"fromUserId\":%s,\"fromRole\":\"%s\",\"timestamp\":%d}",
                escapeJson(displayContent),
                userIdStr != null ? userIdStr : "0",
                role != null ? role : "DM",
                System.currentTimeMillis()
        );

        int sentCount = broadcastToRole("ADMIN", notificationJson);
        System.out.println("[WebSocket] DM呼叫前台 -> 内容: " + displayContent + ", 已推送管理员数: " + sentCount);

        String confirmJson = String.format(
                "{\"type\":\"CALL_CONFIRM\",\"message\":\"呼叫已发送，请等待前台响应\",\"timestamp\":%d}",
                System.currentTimeMillis()
        );
        senderSession.sendMessage(new TextMessage(confirmJson));
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        URI uri = session.getUri();
        Integer userId = extractParam(uri, "userId");
        if (userId != null) {
            onlineSessions.remove(userId);
            System.out.println("[WebSocket] 用户下线 -> userId=" + userId + ", 当前在线: " + onlineSessions.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("[WebSocket] 传输错误: " + exception.getMessage());
    }

    /*
     * ==================== 公共静态方法（供其他 Service / Controller 调用） ====================
     */

    /**
     * 向指定用户推送消息
     */
    public static boolean sendToUser(Integer userId, String jsonMessage) {
        UserSession us = onlineSessions.get(userId);
        if (us != null && us.session.isOpen()) {
            try {
                us.session.sendMessage(new TextMessage(jsonMessage));
                return true;
            } catch (IOException e) {
                System.err.println("[WebSocket] 推送给 userId=" + userId + " 失败: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 向所有在线的指定角色用户广播消息
     */
    public static int broadcastToRole(String role, String jsonMessage) {
        int sent = 0;
        for (UserSession us : onlineSessions.values()) {
            if (role.equalsIgnoreCase(us.role) && us.session.isOpen()) {
                try {
                    us.session.sendMessage(new TextMessage(jsonMessage));
                    sent++;
                } catch (IOException e) {
                    System.err.println("[WebSocket] 广播给 userId=" + us.userId + " 失败: " + e.getMessage());
                }
            }
        }
        return sent;
    }

    /**
     * 向指定 DM 发送场次分配通知（保留向后兼容）
     */
    public static void sendSessionAssignmentNotification(Integer dmId, Integer sessionId, String scriptName,
            String startTime) {
        String message = String.format(
                "{\"type\":\"SESSION_ASSIGNMENT\",\"message\":\"您被分配到新场次\",\"sessionId\":%d,\"scriptName\":\"%s\",\"startTime\":\"%s\"}",
                sessionId, scriptName, startTime);
        sendToUser(dmId, message);
    }

    /** 获取当前在线用户总数 */
    public static int getOnlineCount() {
        return onlineSessions.size();
    }

    /* ==================== 工具方法 ==================== */

    private Integer extractParam(URI uri, String param) {
        if (uri == null)
            return null;
        String query = uri.getQuery();
        if (query == null)
            return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                try {
                    return Integer.parseInt(kv[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String extractStringParam(URI uri, String param) {
        if (uri == null)
            return null;
        String query = uri.getQuery();
        if (query == null)
            return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }

    /**
     * 从简单 JSON 字符串中提取字段值（轻量级，避免引入 Jackson 依赖到这个静态工具类）
     */
    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx < 0)
            return null;
        int colonIdx = idx + search.length();
        while (colonIdx < json.length() && json.charAt(colonIdx) == ' ') colonIdx++;
        if (colonIdx >= json.length() || json.charAt(colonIdx) != ':') return null;
        colonIdx++;
        while (colonIdx < json.length() && json.charAt(colonIdx) == ' ') colonIdx++;
        if (colonIdx >= json.length() || json.charAt(colonIdx) != '"') return null;
        int start = colonIdx + 1;
        int end = json.indexOf("\"", start);
        if (end < 0)
            return null;
        return json.substring(start, end);
    }

    private String extractJsonFieldOrNumber(String json, String field) {
        String strResult = extractJsonField(json, field);
        if (strResult != null) return strResult;
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colonIdx = idx + search.length();
        while (colonIdx < json.length() && json.charAt(colonIdx) == ' ') colonIdx++;
        if (colonIdx >= json.length() || json.charAt(colonIdx) != ':') return null;
        colonIdx++;
        while (colonIdx < json.length() && json.charAt(colonIdx) == ' ') colonIdx++;
        int start = colonIdx;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '.')) {
            end++;
        }
        if (end == start) return null;
        return json.substring(start, end);
    }
}
