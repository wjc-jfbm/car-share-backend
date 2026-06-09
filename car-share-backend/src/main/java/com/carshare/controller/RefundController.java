package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.Refund;
import com.carshare.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/refund")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/apply")
    public Result<?> applyRefund(@RequestBody Refund refund,
                                 @RequestAttribute("userId") Long userId) {
        refund.setUserId(userId);
        boolean success = refundService.applyRefund(refund);
        return success ? Result.success(null, "退款申请已提交") : Result.fail("退款申请失败");
    }

    @PostMapping("/{id}/review")
    public Result<?> reviewRefund(@PathVariable Long id,
                                  @RequestAttribute("userId") Long userId,
                                  @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String rejectReason = (String) params.get("rejectReason");
        boolean success = refundService.reviewRefund(id, userId, status, rejectReason);
        return success ? Result.success(null, "审核成功") : Result.fail("审核失败");
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyRefunds(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(refundService.getMyRefunds(userId, page, pageSize));
    }

    @GetMapping("/car/{carId}")
    public Result<Map<String, Object>> getCarRefunds(@PathVariable Long carId) {
        return Result.success(refundService.getCarRefunds(carId));
    }

    @GetMapping("/car/{carId}/pending")
    public Result<List<Refund>> getPendingRefunds(@PathVariable Long carId,
                                                   @RequestAttribute("userId") Long userId) {
        return Result.success(refundService.getPendingRefunds(carId, userId));
    }
}
