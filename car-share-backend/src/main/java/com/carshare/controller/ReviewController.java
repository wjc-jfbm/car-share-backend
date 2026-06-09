package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.Review;
import com.carshare.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Result<?> createReview(@RequestAttribute("userId") Long userId,
                                  @RequestBody Review review) {
        review.setFromUserId(userId);
        boolean success = reviewService.createReview(review);
        return success ? Result.success(null, "评价成功") : Result.fail("评价失败，可能已评价过");
    }

    @GetMapping("/car/{carId}")
    public Result<?> getReviewsByCarId(@PathVariable Long carId) {
        return Result.success(reviewService.getReviewsByCarId(carId));
    }

    @GetMapping("/user/{userId}")
    public Result<?> getReviewsByUserId(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.getReviewsByToUserIdPage(userId, page, pageSize));
    }

    @GetMapping("/stats/{userId}")
    public Result<?> getUserReviewStats(@PathVariable Long userId) {
        return Result.success(reviewService.getUserReviewStats(userId));
    }

    @GetMapping("/check")
    public Result<?> checkReviewed(@RequestParam Long carId,
                                   @RequestParam Long toUserId,
                                   @RequestAttribute("userId") Long userId) {
        boolean hasReviewed = reviewService.hasReviewed(carId, userId, toUserId);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("hasReviewed", hasReviewed);
        return Result.success(data);
    }
}
