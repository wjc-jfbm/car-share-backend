package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.Logistics;
import com.carshare.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @GetMapping("/car/{carId}")
    public Result<?> getLogisticsByCarId(@PathVariable Long carId) {
        Map<String, Object> result = logisticsService.getLogisticsByCarId(carId);
        if (result == null) {
            return Result.fail("拼车不存在");
        }
        return Result.success(result);
    }

    @PostMapping
    public Result<?> createLogistics(@RequestAttribute("userId") Long userId,
                                     @RequestBody Map<String, String> params) {
        String carIdStr = params.get("car_id");
        String expressNo = params.get("express_no");
        String expressCompany = params.get("express_company");
        if (carIdStr == null || carIdStr.trim().isEmpty()) {
            return Result.fail("拼车ID不能为空");
        }
        if (expressNo == null || expressNo.trim().isEmpty()) {
            return Result.fail("快递单号不能为空");
        }
        if (expressCompany == null || expressCompany.trim().isEmpty()) {
            return Result.fail("快递公司不能为空");
        }

        Logistics logistics = new Logistics();
        logistics.setCarId(Long.parseLong(carIdStr));
        logistics.setExpressNo(expressNo.trim());
        logistics.setExpressCompany(expressCompany);
        logistics.setExpressCompanyCode(params.getOrDefault("express_company_code", ""));
        logistics.setSenderName(params.getOrDefault("sender_name", ""));
        logistics.setSenderPhone(params.getOrDefault("sender_phone", ""));
        logistics.setSenderAddress(params.getOrDefault("sender_address", ""));
        logistics.setReceiverName(params.getOrDefault("receiver_name", ""));
        logistics.setReceiverPhone(params.getOrDefault("receiver_phone", ""));
        logistics.setReceiverAddress(params.getOrDefault("receiver_address", ""));
        logistics.setRemark(params.getOrDefault("remark", ""));

        Long id = logisticsService.createLogistics(userId, logistics);
        if (id == null) {
            return Result.fail("创建失败，无权操作或拼车状态不正确");
        }
        return Result.success(id, "发货成功");
    }

    @PutMapping("/{id}")
    public Result<?> updateLogistics(@PathVariable Long id,
                                     @RequestAttribute("userId") Long userId,
                                     @RequestBody Map<String, Object> params) {
        Logistics logistics = new Logistics();
        if (params.get("status") != null) {
            logistics.setStatus(Integer.parseInt(params.get("status").toString()));
        }
        if (params.get("express_no") != null) {
            logistics.setExpressNo(params.get("express_no").toString());
        }
        if (params.get("express_company") != null) {
            logistics.setExpressCompany(params.get("express_company").toString());
        }
        if (params.get("receiver_name") != null) {
            logistics.setReceiverName(params.get("receiver_name").toString());
        }
        if (params.get("receiver_phone") != null) {
            logistics.setReceiverPhone(params.get("receiver_phone").toString());
        }
        if (params.get("receiver_address") != null) {
            logistics.setReceiverAddress(params.get("receiver_address").toString());
        }

        boolean success = logisticsService.updateLogistics(id, userId, logistics);
        return success ? Result.success(null, "更新成功") : Result.fail("更新失败");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id,
                                  @RequestAttribute("userId") Long userId,
                                  @RequestBody Map<String, Integer> params) {
        boolean success = logisticsService.updateStatus(id, userId, params.get("status"));
        return success ? Result.success(null, "状态更新成功") : Result.fail("更新失败，无权操作");
    }

    @GetMapping("/list")
    public Result<?> getLogisticsList(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(logisticsService.getLogisticsList(page, pageSize));
    }
}
