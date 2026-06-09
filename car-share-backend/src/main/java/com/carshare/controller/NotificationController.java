package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<?> getMyNotifications(@RequestAttribute("userId") Long userId,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(notificationService.getMyNotifications(userId, page, pageSize));
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount(@RequestAttribute("userId") Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id,
                                @RequestAttribute("userId") Long userId) {
        boolean success = notificationService.markAsRead(id, userId);
        return success ? Result.success(null, "已标记已读") : Result.fail("操作失败");
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead(@RequestAttribute("userId") Long userId) {
        boolean success = notificationService.markAllAsRead(userId);
        return success ? Result.success(null, "全部已读") : Result.fail("操作失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteNotification(@PathVariable Long id,
                                        @RequestAttribute("userId") Long userId) {
        boolean success = notificationService.deleteNotification(id, userId);
        return success ? Result.success(null, "已删除") : Result.fail("删除失败");
    }
}
