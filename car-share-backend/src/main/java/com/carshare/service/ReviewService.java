package com.carshare.service;

import com.carshare.entity.Review;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    boolean createReview(Review review);
    List<Review> getReviewsByCarId(Long carId);
    List<Review> getReviewsByToUserId(Long userId);
    Map<String, Object> getReviewsByToUserIdPage(Long userId, Integer page, Integer pageSize);
    boolean hasReviewed(Long carId, Long fromUserId, Long toUserId);
    Map<String, Object> getUserReviewStats(Long userId);
}
