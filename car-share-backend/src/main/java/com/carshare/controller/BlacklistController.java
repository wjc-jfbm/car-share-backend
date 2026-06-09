package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.Blacklist;
import com.carshare.entity.Report;
import com.carshare.service.BlacklistService;
import com.carshare.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    @PostMapping("/add")
    public Result<?> addBlacklist(@RequestBody Map<String, Object> params,
                                  @RequestAttribute("userId") Long userId) {
        Long blockedUserId = Long.valueOf(params.get("blockedUserId").toString());
        String reason = (String) params.getOrDefault("reason", "");
        boolean success = blacklistService.addBlacklist(userId, blockedUserId, reason);
        return success ? Result.success(null, "拉黑成功") : Result.fail("拉黑失败");
    }

    @DeleteMapping("/{blockedUserId}")
    public Result<?> removeBlacklist(@PathVariable Long blockedUserId,
                                     @RequestAttribute("userId") Long userId) {
        boolean success = blacklistService.removeBlacklist(userId, blockedUserId);
        return success ? Result.success(null, "取消拉黑成功") : Result.fail("取消拉黑失败");
    }

    @GetMapping("/my")
    public Result<List<Blacklist>> getMyBlacklist(@RequestAttribute("userId") Long userId) {
        return Result.success(blacklistService.getMyBlacklist(userId));
    }

    @GetMapping("/check/{targetUserId}")
    public Result<Boolean> isBlocked(@PathVariable Long targetUserId,
                                     @RequestAttribute("userId") Long userId) {
        return Result.success(blacklistService.isBlocked(userId, targetUserId));
    }
}

@RestController
@RequestMapping("/api/report")
class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/create")
    public Result<?> createReport(@RequestBody Report report,
                                  @RequestAttribute("userId") Long userId) {
        report.setUserId(userId);
        boolean success = reportService.createReport(report);
        return success ? Result.success(null, "举报已提交") : Result.fail("举报提交失败");
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyReports(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.getMyReports(userId, page, pageSize));
    }

    @GetMapping("/pending")
    public Result<Map<String, Object>> getPendingReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.getPendingReports(page, pageSize));
    }

    @PostMapping("/{id}/handle")
    public Result<?> handleReport(@PathVariable Long id,
                                  @RequestBody Map<String, Object> params,
                                  @RequestAttribute("userId") Long adminId) {
        Integer status = (Integer) params.get("status");
        String handleResult = (String) params.get("handleResult");
        boolean success = reportService.handleReport(id, adminId, status, handleResult);
        return success ? Result.success(null, "处理成功") : Result.fail("处理失败");
    }
}
