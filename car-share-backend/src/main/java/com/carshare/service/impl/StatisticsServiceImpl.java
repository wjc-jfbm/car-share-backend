package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.common.enums.CarStatus;
import com.carshare.entity.*;
import com.carshare.mapper.*;
import com.carshare.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarMemberMapper carMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private CarOrderMapper carOrderMapper;

    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<Car> createdWrapper = new LambdaQueryWrapper<>();
        createdWrapper.eq(Car::getUserId, userId);
        long createdCount = carMapper.selectCount(createdWrapper);

        LambdaQueryWrapper<Car> completedCreatedWrapper = new LambdaQueryWrapper<>();
        completedCreatedWrapper.eq(Car::getUserId, userId)
                .in(Car::getStatus, CarStatus.SETTLED.getCode(), CarStatus.SHIPPED.getCode(), CarStatus.COMPLETED.getCode());
        long completedCreatedCount = carMapper.selectCount(completedCreatedWrapper);

        LambdaQueryWrapper<CarMember> joinedWrapper = new LambdaQueryWrapper<>();
        joinedWrapper.eq(CarMember::getUserId, userId).eq(CarMember::getIsOwner, 0);
        long joinedCount = carMemberMapper.selectCount(joinedWrapper);

        LambdaQueryWrapper<CarMember> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(CarMember::getUserId, userId).eq(CarMember::getPayStatus, 1);
        long paidCount = carMemberMapper.selectCount(paidWrapper);

        LambdaQueryWrapper<Review> reviewToWrapper = new LambdaQueryWrapper<>();
        reviewToWrapper.eq(Review::getToUserId, userId);
        long reviewCount = reviewMapper.selectCount(reviewToWrapper);

        Double avgRating = reviewMapper.selectAvgRatingByToUserId(userId);

        User user = userMapper.selectById(userId);

        result.put("createdCount", createdCount);
        result.put("completedCreatedCount", completedCreatedCount);
        result.put("joinedCount", joinedCount);
        result.put("paidCount", paidCount);
        result.put("reviewCount", reviewCount);
        result.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
        result.put("creditScore", user != null ? user.getCreditScore() : 0);
        result.put("totalTransactions", user != null ? user.getTotalTransactions() : 0);
        result.put("successTransactions", user != null ? user.getSuccessTransactions() : 0);

        return result;
    }

    @Override
    public Map<String, Object> getCarStatistics(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", car.getTotalCount());
        result.put("currentCount", car.getCurrentCount());
        result.put("participationRate", car.getTotalCount() > 0
                ? Math.round((double) car.getCurrentCount() / car.getTotalCount() * 100) : 0);

        LambdaQueryWrapper<CarMember> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(CarMember::getCarId, carId).eq(CarMember::getPayStatus, 1);
        long paidCount = carMemberMapper.selectCount(paidWrapper);

        LambdaQueryWrapper<CarMember> totalMemberWrapper = new LambdaQueryWrapper<>();
        totalMemberWrapper.eq(CarMember::getCarId, carId);
        long memberCount = carMemberMapper.selectCount(totalMemberWrapper);

        result.put("paidCount", paidCount);
        result.put("unpaidCount", memberCount - paidCount);
        result.put("paymentRate", memberCount > 0
                ? Math.round((double) paidCount / memberCount * 100) : 0);
        result.put("successRate", car.getSuccessRate());

        return result;
    }

    @Override
    public Map<String, Object> getPlatformOverview() {
        Map<String, Object> result = new HashMap<>();

        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        long totalCars = carMapper.selectCount(new LambdaQueryWrapper<>());

        LambdaQueryWrapper<Car> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Car::getStatus, CarStatus.RECRUITING.getCode());
        long activeCars = carMapper.selectCount(activeWrapper);

        LambdaQueryWrapper<Car> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.in(Car::getStatus, CarStatus.SETTLED.getCode(), CarStatus.SHIPPED.getCode(), CarStatus.COMPLETED.getCode());
        long completedCars = carMapper.selectCount(completedWrapper);

        long totalMembers = carMemberMapper.selectCount(new LambdaQueryWrapper<>());

        result.put("totalUsers", totalUsers);
        result.put("totalCars", totalCars);
        result.put("activeCars", activeCars);
        result.put("completedCars", completedCars);
        result.put("totalMembers", totalMembers);

        return result;
    }

    @Override
    public Map<String, Object> getUserCreditTrend(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trend = new ArrayList<>();

        User user = userMapper.selectById(userId);
        if (user == null) {
            result.put("trend", trend);
            return result;
        }

        // 基于评价记录构建信用变化趋势
        // 按天聚合最近的信用变化
        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> dailyScoreChange = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyScoreChange.put(today.minusDays(i), 0);
        }

        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getToUserId, userId)
                .ge(Review::getCreatedAt, today.minusDays(7).atStartOfDay())
                .orderByAsc(Review::getCreatedAt);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);

        for (Review review : reviews) {
            LocalDate reviewDate = review.getCreatedAt().toLocalDate();
            if (dailyScoreChange.containsKey(reviewDate)) {
                int delta;
                if (review.getRating() >= 5) delta = 2;
                else if (review.getRating() >= 4) delta = 1;
                else if (review.getRating() >= 3) delta = 0;
                else if (review.getRating() >= 2) delta = -3;
                else delta = -5;
                dailyScoreChange.merge(reviewDate, delta, Integer::sum);
            }
        }

        // 从当前信用分反推每日变化
        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 60;
        List<LocalDate> sortedDays = new ArrayList<>(dailyScoreChange.keySet());
        Collections.sort(sortedDays);

        int cumulativeScore = currentScore;
        // 从后往前推算每日的信用分
        for (int i = sortedDays.size() - 1; i >= 0; i--) {
            LocalDate date = sortedDays.get(i);
            int change = dailyScoreChange.getOrDefault(date, 0);
            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("score", Math.max(0, cumulativeScore));
            trend.add(0, point);
            cumulativeScore -= change;
        }

        result.put("trend", trend);
        return result;
    }

    @Override
    public Map<String, Object> getCarStatusDistribution(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> distribution = new ArrayList<>();

        String[] statusNames = {CarStatus.RECRUITING.getLabel(), CarStatus.CLOSED.getLabel(), CarStatus.COMPLETED.getLabel(), CarStatus.CANCELLED.getLabel()};
        int[] statusValues = {CarStatus.RECRUITING.getCode(), CarStatus.CLOSED.getCode(), CarStatus.COMPLETED.getCode(), CarStatus.CANCELLED.getCode()};

        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getUserId, userId);
        List<CarMember> myMembers = carMemberMapper.selectList(memberWrapper);

        List<Long> carIds = myMembers.stream()
                .map(CarMember::getCarId).distinct().collect(Collectors.toList());

        for (int i = 0; i < statusNames.length; i++) {
            long count = 0;
            if (!carIds.isEmpty()) {
                LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
                carWrapper.in(Car::getId, carIds).eq(Car::getStatus, statusValues[i]);
                count = carMapper.selectCount(carWrapper);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", count);
            distribution.add(item);
        }

        result.put("distribution", distribution);
        return result;
    }

    @Override
    public Map<String, Object> getMonthlyTrend(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trend = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            LocalDateTime startDateTime = monthStart.atStartOfDay();
            LocalDateTime endDateTime = monthEnd.atTime(LocalTime.MAX);

            LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(CarMember::getUserId, userId)
                         .ge(CarMember::getJoinTime, startDateTime)
                         .le(CarMember::getJoinTime, endDateTime);
            long joinCount = carMemberMapper.selectCount(memberWrapper);

            Map<String, Object> point = new HashMap<>();
            point.put("month", monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()));
            point.put("count", joinCount);
            trend.add(point);
        }

        result.put("trend", trend);
        return result;
    }
}
