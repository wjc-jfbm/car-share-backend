package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.service.CarFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class CarFavoriteController {

    @Autowired
    private CarFavoriteService carFavoriteService;

    @PostMapping("/{carId}")
    public Result<?> addFavorite(@PathVariable Long carId,
                                 @RequestAttribute("userId") Long userId) {
        boolean success = carFavoriteService.addFavorite(carId, userId);
        return success ? Result.success(null, "收藏成功") : Result.fail("收藏失败");
    }

    @DeleteMapping("/{carId}")
    public Result<?> removeFavorite(@PathVariable Long carId,
                                    @RequestAttribute("userId") Long userId) {
        boolean success = carFavoriteService.removeFavorite(carId, userId);
        return success ? Result.success(null, "取消收藏成功") : Result.fail("取消收藏失败");
    }

    @GetMapping("/check/{carId}")
    public Result<Boolean> isFavorite(@PathVariable Long carId,
                                      @RequestAttribute("userId") Long userId) {
        return Result.success(carFavoriteService.isFavorite(carId, userId));
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyFavorites(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(carFavoriteService.getMyFavorites(userId, page, pageSize));
    }
}
