package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Review;
import com.carshare.entity.User;
import com.carshare.mapper.ReviewMapper;
import com.carshare.mapper.UserMapper;
import com.carshare.common.utils.PageResult;
import com.carshare.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean createReview(Review review) {
        boolean exists = hasReviewed(review.getCarId(), review.getFromUserId(), review.getToUserId());
        if (exists) {
            return false;
        }

        reviewMapper.insert(review);
        updateUserCredit(review.getToUserId(), review.getRating());
        return true;
    }

    private void updateUserCredit(Long userId, int rating) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 60;
        int delta;
        if (rating >= 5) delta = 2;
        else if (rating >= 4) delta = 1;
        else if (rating >= 3) delta = 0;
        else if (rating >= 2) delta = -3;
        else delta = -5;

        int newScore = Math.max(0, Math.min(100, currentScore + delta));
        user.setCreditScore(newScore);

        if (newScore >= 90) user.setCreditLevel(5);
        else if (newScore >= 75) user.setCreditLevel(4);
        else if (newScore >= 60) user.setCreditLevel(3);
        else if (newScore >= 40) user.setCreditLevel(2);
        else user.setCreditLevel(1);

        userMapper.updateById(user);
    }

    @Override
    public List<Review> getReviewsByCarId(Long carId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCarId, carId).orderByDesc(Review::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    @Override
    public List<Review> getReviewsByToUserId(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getToUserId, userId).orderByDesc(Review::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getReviewsByToUserIdPage(Long userId, Integer page, Integer pageSize) {
        Page<Review> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getToUserId, userId).orderByDesc(Review::getCreatedAt);
        return PageResult.of(reviewMapper.selectPage(pageObj, wrapper));
    }

    @Override
    public boolean hasReviewed(Long carId, Long fromUserId, Long toUserId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCarId, carId)
                .eq(Review::getFromUserId, fromUserId)
                .eq(Review::getToUserId, toUserId);
        return reviewMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Map<String, Object> getUserReviewStats(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getToUserId, userId);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", reviews.size());
        stats.put("avgRating", Math.round(avgRating * 10) / 10.0);
        stats.put("fiveStar", reviews.stream().filter(r -> r.getRating() == 5).count());
        stats.put("fourStar", reviews.stream().filter(r -> r.getRating() == 4).count());
        stats.put("threeStar", reviews.stream().filter(r -> r.getRating() == 3).count());
        stats.put("twoStar", reviews.stream().filter(r -> r.getRating() == 2).count());
        stats.put("oneStar", reviews.stream().filter(r -> r.getRating() == 1).count());
        return stats;
    }
}
