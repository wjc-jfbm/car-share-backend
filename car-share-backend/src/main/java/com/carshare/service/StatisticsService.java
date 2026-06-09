package com.carshare.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getUserStatistics(Long userId);
    Map<String, Object> getCarStatistics(Long carId);
    Map<String, Object> getPlatformOverview();
    Map<String, Object> getUserCreditTrend(Long userId);
    Map<String, Object> getCarStatusDistribution(Long userId);
    Map<String, Object> getMonthlyTrend(Long userId);
}
