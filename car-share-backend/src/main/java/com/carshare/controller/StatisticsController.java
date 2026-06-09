package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/user")
    public Result<?> getUserStatistics(@RequestAttribute("userId") Long userId) {
        return Result.success(statisticsService.getUserStatistics(userId));
    }

    @GetMapping("/car/{carId}")
    public Result<?> getCarStatistics(@PathVariable Long carId) {
        return Result.success(statisticsService.getCarStatistics(carId));
    }

    @GetMapping("/platform")
    public Result<?> getPlatformOverview() {
        return Result.success(statisticsService.getPlatformOverview());
    }

    @GetMapping("/credit-trend")
    public Result<?> getCreditTrend(@RequestAttribute("userId") Long userId) {
        return Result.success(statisticsService.getUserCreditTrend(userId));
    }

    @GetMapping("/car-status-distribution")
    public Result<?> getCarStatusDistribution(@RequestAttribute("userId") Long userId) {
        return Result.success(statisticsService.getCarStatusDistribution(userId));
    }

    @GetMapping("/monthly-trend")
    public Result<?> getMonthlyTrend(@RequestAttribute("userId") Long userId) {
        return Result.success(statisticsService.getMonthlyTrend(userId));
    }
}
