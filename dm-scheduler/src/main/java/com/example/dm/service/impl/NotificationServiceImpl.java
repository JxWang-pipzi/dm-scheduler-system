package com.example.dm.service.impl;

import com.example.dm.entity.Notification;
import com.example.dm.mapper.NotificationMapper;
import com.example.dm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public boolean sendNotification(Notification notification) {
        return notificationMapper.insert(notification) > 0;
    }

    @Override
    public Notification getNotificationById(Integer id) {
        return notificationMapper.selectById(id);
    }

    @Override
    public List<Notification> getNotificationsByUser(Integer userId) {
        return notificationMapper.selectByUser(userId);
    }

    @Override
    public List<Notification> getSystemNotifications() {
        return notificationMapper.selectSystemNotifications();
    }

    @Override
    public boolean markAsRead(Integer id) {
        return notificationMapper.updateReadStatus(id) > 0;
    }

    @Override
    public int countUnreadByUser(Integer userId) {
        return notificationMapper.countUnreadByUser(userId);
    }
}
