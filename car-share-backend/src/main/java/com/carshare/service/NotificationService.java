package com.carshare.service;

import com.carshare.entity.Notification;
import java.util.List;
import java.util.Map;

public interface NotificationService {
    void sendNotification(Long userId, Long carId, String title, String content, Integer type);
    void sendToCarMembers(Long carId, Long excludeUserId, String title, String content, Integer type);
    Map<String, Object> getMyNotifications(Long userId, Integer page, Integer pageSize);
    long getUnreadCount(Long userId);
    boolean markAsRead(Long notificationId, Long userId);
    boolean markAllAsRead(Long userId);
    boolean deleteNotification(Long notificationId, Long userId);
}
