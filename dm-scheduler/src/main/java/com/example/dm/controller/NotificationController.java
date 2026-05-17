package com.example.dm.controller;

import com.example.dm.entity.Notification;
import com.example.dm.service.NotificationService;
import com.example.dm.util.AuthContext;
import com.example.dm.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public Result sendNotification(@RequestBody Notification notification) {
        if (notification == null || notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            return Result.error(4000, "通知标题不能为空");
        }
        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            return Result.error(4000, "通知内容不能为空");
        }
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        boolean success = notificationService.sendNotification(notification);
        return success ? Result.success() : Result.error("发送通知失败");
    }

    @GetMapping("/list")
    public Result getNotificationsByUser(@RequestParam Integer userId) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (!AuthContext.isAdmin()) {
            userId = currentUserId;
        }
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return Result.success(notifications);
    }

    @GetMapping("/system")
    public Result getSystemNotifications() {
        List<Notification> notifications = notificationService.getSystemNotifications();
        return Result.success(notifications);
    }

    @PutMapping("/read/{id}")
    public Result markAsRead(@PathVariable Integer id) {
        Notification notification = notificationService.getNotificationById(id);
        if (notification == null) {
            return Result.error(4004, "通知不存在");
        }
        if (!AuthContext.isAdmin()) {
            Integer currentUserId = AuthContext.getUserId();
            if (currentUserId == null) {
                return Result.error(401, "请先登录");
            }
            Integer targetUserId = notification.getUserId();
            if (targetUserId == null) {
                return Result.error(403, "系统通知仅管理员可变更状态");
            }
            if (!currentUserId.equals(targetUserId)) {
                return Result.error(403, "无权操作他人的通知");
            }
        }
        boolean success = notificationService.markAsRead(id);
        return success ? Result.success() : Result.error("标记已读失败");
    }

    @GetMapping("/unread-count")
    public Result countUnreadByUser(@RequestParam Integer userId) {
        Integer currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        if (!AuthContext.isAdmin()) {
            userId = currentUserId;
        }
        int count = notificationService.countUnreadByUser(userId);
        return Result.success(count);
    }
}
