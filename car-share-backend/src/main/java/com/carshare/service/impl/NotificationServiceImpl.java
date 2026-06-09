package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.CarMember;
import com.carshare.entity.Notification;
import com.carshare.mapper.CarMemberMapper;
import com.carshare.mapper.NotificationMapper;
import com.carshare.common.utils.PageResult;
import com.carshare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private CarMemberMapper carMemberMapper;

    @Override
    public void sendNotification(Long userId, Long carId, String title, String content, Integer type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setCarId(carId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public void sendToCarMembers(Long carId, Long excludeUserId, String title, String content, Integer type) {
        LambdaQueryWrapper<CarMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarMember::getCarId, carId);
        if (excludeUserId != null) {
            wrapper.ne(CarMember::getUserId, excludeUserId);
        }
        List<CarMember> members = carMemberMapper.selectList(wrapper);

        for (CarMember member : members) {
            sendNotification(member.getUserId(), carId, title, content, type);
        }
    }

    @Override
    public Map<String, Object> getMyNotifications(Long userId, Integer page, Integer pageSize) {
        Page<Notification> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreatedAt);

        return PageResult.of(notificationMapper.selectPage(pageObj, wrapper));
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getId, notificationId).eq(Notification::getUserId, userId);
        return notificationMapper.update(notification, queryWrapper) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return notificationMapper.update(notification, queryWrapper) > 0;
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, notificationId).eq(Notification::getUserId, userId);
        return notificationMapper.delete(wrapper) > 0;
    }
}
