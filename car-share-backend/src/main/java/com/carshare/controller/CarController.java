package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.Car;
import com.carshare.entity.CarMember;
import com.carshare.service.CarService;
import com.carshare.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/car")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private MatchService matchService;

    @GetMapping("/list")
    public Result<?> getCarList(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) Integer status,
                                @RequestParam(required = false) String keyword) {
        return Result.success(carService.getCarList(page, pageSize, status, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<?> getCarDetail(@PathVariable Long id) {
        return Result.success(carService.getCarDetail(id));
    }

    @PostMapping
    public Result<?> createCar(@RequestAttribute("userId") Long userId,
                               @RequestBody Car car) {
        Long carId = carService.createCar(userId, car);
        return Result.success(carId, "创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> updateCar(@PathVariable Long id,
                               @RequestAttribute("userId") Long userId,
                               @RequestBody Car car) {
        car.setId(id);
        boolean success = carService.updateCar(car);
        return success ? Result.success(null, "更新成功") : Result.fail("更新失败");
    }

    @PostMapping("/{id}/join")
    public Result<?> joinCar(@PathVariable Long id,
                             @RequestAttribute("userId") Long userId,
                             @RequestBody CarMember member) {
        boolean success = carService.joinCar(id, userId, member);
        return success ? Result.success(null, "参与成功") : Result.fail("参与失败，请检查是否已参与或拼车已满");
    }

    @PostMapping("/{id}/close")
    public Result<?> closeCar(@PathVariable Long id,
                              @RequestAttribute("userId") Long userId) {
        boolean success = carService.closeCar(id, userId);
        return success ? Result.success(null, "已截止") : Result.fail("操作失败");
    }

    @PostMapping("/{id}/claim")
    public Result<?> claimItem(@PathVariable Long id,
                               @RequestAttribute("userId") Long userId,
                               @RequestBody Map<String, String> params) {
        boolean success = carService.claimItem(id, userId,
                params.get("claimedVersion"), params.get("claimedCard"));
        return success ? Result.success(null, "认领成功") : Result.fail("认领失败");
    }

    @PostMapping("/evidence/upload")
    public Result<?> uploadPayEvidence(@RequestAttribute("userId") Long userId,
                                       @RequestBody Map<String, String> params) {
        Long carMemberId = Long.parseLong(params.get("carMemberId"));
        boolean success = carService.uploadPayEvidence(carMemberId, userId, params.get("evidenceUrl"));
        return success ? Result.success(null, "凭证上传成功") : Result.fail("上传失败");
    }

    @PostMapping("/evidence/review")
    public Result<?> reviewPayEvidence(@RequestAttribute("userId") Long userId,
                                       @RequestBody Map<String, String> params) {
        Long carMemberId = Long.parseLong(params.get("carMemberId"));
        Integer status = Integer.parseInt(params.get("status"));
        boolean success = carService.reviewPayEvidence(carMemberId, userId, status, params.get("rejectReason"));
        return success ? Result.success(null, "审核完成") : Result.fail("审核失败");
    }

    @PostMapping("/{id}/distribute")
    public Result<?> distribute(@PathVariable Long id,
                                @RequestAttribute("userId") Long userId) {
        Map<String, Object> result = carService.distribute(id, userId);
        return Result.success(result, "分配完成");
    }

    @PostMapping("/{id}/complete")
    public Result<?> completeCar(@PathVariable Long id,
                                 @RequestAttribute("userId") Long userId) {
        boolean success = carService.completeCar(id, userId);
        return success ? Result.success(null, "已完成") : Result.fail("操作失败");
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelCar(@PathVariable Long id,
                               @RequestAttribute("userId") Long userId) {
        boolean success = carService.cancelCar(id, userId);
        return success ? Result.success(null, "已取消") : Result.fail("取消失败");
    }

    @GetMapping("/my")
    public Result<?> getMyCars(@RequestAttribute("userId") Long userId,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(carService.getMyCars(userId, page, pageSize));
    }

    @GetMapping("/my-all")
    public Result<?> getMyAllCars(@RequestAttribute("userId") Long userId,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(carService.getMyAllCars(userId, page, pageSize));
    }

    @GetMapping("/joined")
    public Result<?> getMyJoinedCars(@RequestAttribute("userId") Long userId,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(carService.getMyJoinedCars(userId, page, pageSize));
    }

    @GetMapping("/recommend")
    public Result<?> getRecommendedCars(@RequestAttribute("userId") Long userId,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(matchService.getRecommendedCars(userId, page, pageSize));
    }

    @GetMapping("/match-score")
    public Result<?> getMatchScore(@RequestAttribute("userId") Long userId,
                                   @RequestParam Long carId) {
        return Result.success(matchService.calculateMatchScore(userId, carId));
    }
}
