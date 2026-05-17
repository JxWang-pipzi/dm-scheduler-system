package com.example.dm.mapper;

import com.example.dm.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(Notification notification);
    Notification selectById(Integer id);
    List<Notification> selectByUser(Integer userId);
    List<Notification> selectSystemNotifications();
    int updateReadStatus(Integer id);
    int countUnreadByUser(Integer userId);
}
