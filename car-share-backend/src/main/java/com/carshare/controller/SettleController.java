package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.CarOrder;
import com.carshare.entity.Evidence;
import com.carshare.service.SettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settle")
public class SettleController {

    @Autowired
    private SettleService settleService;

    @GetMapping("/{carId}")
    public Result<?> getSettleInfo(@PathVariable Long carId,
                                   @RequestAttribute("userId") Long userId) {
        Map<String, Object> info = settleService.getSettleInfo(carId, userId);
        if (info == null) {
            return Result.fail("拼车不存在");
        }
        return Result.success(info);
    }

    @GetMapping("/{carId}/orders")
    public Result<?> getOrders(@PathVariable Long carId) {
        List<CarOrder> orders = settleService.getOrdersByCarId(carId);
        return Result.success(orders);
    }

    @PostMapping("/order")
    public Result<?> createOrder(@RequestBody CarOrder order) {
        boolean success = settleService.createOrder(order);
        return success ? Result.success(null, "订单创建成功") : Result.fail("创建失败");
    }

    @PostMapping("/evidence")
    public Result<?> uploadEvidence(@RequestAttribute("userId") Long userId,
                                    @RequestBody Map<String, Object> params) {
        Long carMemberId = Long.parseLong(params.get("carMemberId").toString());
        Long carId = Long.parseLong(params.get("carId").toString());
        Integer type = Integer.parseInt(params.get("type").toString());
        String imageUrl = (String) params.get("imageUrl");
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        boolean success = settleService.uploadEvidence(carMemberId, carId, userId, type, imageUrl, remark);
        return success ? Result.success(null, "凭证上传成功") : Result.fail("上传失败");
    }

    @PostMapping("/evidence/{id}/review")
    public Result<?> reviewEvidence(@PathVariable Long id,
                                    @RequestAttribute("userId") Long userId,
                                    @RequestBody Map<String, Object> params) {
        Integer status = Integer.parseInt(params.get("status").toString());
        String rejectReason = params.get("reject_reason") != null ? params.get("reject_reason").toString() : null;

        boolean success = settleService.reviewEvidence(id, userId, status, rejectReason);
        return success ? Result.success(null, "审核完成") : Result.fail("审核失败");
    }

    @GetMapping("/{carId}/evidences")
    public Result<?> getEvidences(@PathVariable Long carId) {
        List<Evidence> evidences = settleService.getEvidencesByCarId(carId);
        return Result.success(evidences);
    }

    @GetMapping("/my-evidences")
    public Result<?> getMyEvidences(@RequestAttribute("userId") Long userId) {
        List<Evidence> evidences = settleService.getEvidencesByUserId(userId);
        return Result.success(evidences);
    }

    @PostMapping("/order/{id}/settle")
    public Result<?> settleOrder(@PathVariable Long id,
                                 @RequestAttribute("userId") Long userId) {
        boolean success = settleService.settleOrder(id, userId);
        return success ? Result.success(null, "结算完成") : Result.fail("结算失败");
    }

    @GetMapping("/my-orders")
    public Result<?> getMyOrders(@RequestAttribute("userId") Long userId,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(settleService.getMyOrders(userId, page, pageSize));
    }

    @PostMapping("/{carId}/confirm")
    public Result<?> confirmSettle(@PathVariable Long carId,
                                   @RequestAttribute("userId") Long userId) {
        boolean success = settleService.confirmSettle(carId, userId);
        if (success) {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("carId", carId);
            data.put("status", 2);
            return Result.success(data, "结算确认成功，请尽快发货");
        }
        return Result.fail("确认失败，请确保所有成员已完成付款");
    }

    @GetMapping("/{carId}/shipping-info")
    public Result<?> getShippingInfo(@PathVariable Long carId,
                                     @RequestAttribute("userId") Long userId) {
        Map<String, Object> info = settleService.getShippingInfo(carId, userId);
        if (info == null) {
            return Result.fail("拼车不存在或无权查看");
        }
        return Result.success(info);
    }
}
