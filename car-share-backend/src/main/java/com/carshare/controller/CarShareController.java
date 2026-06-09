package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.CarShareRecord;
import com.carshare.service.CarShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class CarShareController {

    @Autowired
    private CarShareService carShareService;

    @PostMapping("/create")
    public Result<CarShareRecord> createShare(@RequestBody Map<String, Object> params,
                                               @RequestAttribute("userId") Long userId) {
        Long carId = Long.valueOf(params.get("carId").toString());
        String shareType = (String) params.getOrDefault("shareType", "friend");
        CarShareRecord record = carShareService.createShareRecord(carId, userId, shareType);
        return Result.success(record, "分享记录创建成功");
    }

    @GetMapping("/code/{shareCode}")
    public Result<CarShareRecord> getShareByCode(@PathVariable String shareCode) {
        CarShareRecord record = carShareService.getShareByCode(shareCode);
        if (record == null) return Result.fail("分享码无效");
        return Result.success(record);
    }

    @PostMapping("/invite")
    public Result<?> recordInvite(@RequestBody Map<String, Object> params,
                                   @RequestAttribute("userId") Long userId) {
        String shareCode = (String) params.get("shareCode");
        boolean success = carShareService.recordInvite(shareCode, userId);
        return success ? Result.success(null, "邀请记录成功") : Result.fail("邀请记录失败");
    }

    @GetMapping("/stats/{carId}")
    public Result<Map<String, Object>> getShareStats(@PathVariable Long carId,
                                                      @RequestAttribute("userId") Long userId) {
        return Result.success(carShareService.getShareStats(carId, userId));
    }
}
