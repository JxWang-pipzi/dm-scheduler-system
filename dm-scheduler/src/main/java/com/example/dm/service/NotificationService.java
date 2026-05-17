package com.example.dm.service;

import com.example.dm.entity.Notification;

import java.util.List;

public interface NotificationService {
    boolean sendNotification(Notification notification);
    Notification getNotificationById(Integer id);
    List<Notification> getNotificationsByUser(Integer userId);
    List<Notification> getSystemNotifications();
    boolean markAsRead(Integer id);
    int countUnreadByUser(Integer userId);
}
