package com.carshare.service;

import com.carshare.entity.Car;
import com.carshare.entity.UserPreference;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MatchService {
    BigDecimal calculateMatchScore(Long userId, Long carId);
    BigDecimal calculateMatchScore(UserPreference preference, Car car);
    BigDecimal calculateSuccessRate(Car car);
    Map<String, Object> getRecommendedCars(Long userId, Integer page, Integer pageSize);
    Map<String, Integer> smartDistribute(Long carId);
}
