package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.FeeDetail;
import com.carshare.service.FeeDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fee")
public class FeeDetailController {

    @Autowired
    private FeeDetailService feeDetailService;

    @PostMapping("/calculate/{carId}")
    public Result<?> calculateFee(@PathVariable Long carId,
                                  @RequestBody Map<String, Object> params) {
        Integer shippingFeeType = (Integer) params.getOrDefault("shippingFeeType", 0);
        java.math.BigDecimal shippingFee = new java.math.BigDecimal(params.getOrDefault("shippingFee", "0").toString());
        boolean success = feeDetailService.calculateFee(carId, shippingFeeType, shippingFee);
        return success ? Result.success(null, "费用计算成功") : Result.fail("费用计算失败");
    }

    @GetMapping("/member/{carId}")
    public Result<FeeDetail> getMemberFee(@PathVariable Long carId,
                                          @RequestAttribute("userId") Long userId) {
        FeeDetail fee = feeDetailService.getMemberFee(carId, userId);
        return fee != null ? Result.success(fee) : Result.fail("暂无费用明细");
    }

    @GetMapping("/car/{carId}")
    public Result<List<FeeDetail>> getCarFees(@PathVariable Long carId) {
        return Result.success(feeDetailService.getCarFees(carId));
    }
}
