package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.UserAddress;
import com.carshare.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("/list")
    public Result<List<UserAddress>> getMyAddresses(@RequestAttribute("userId") Long userId) {
        return Result.success(userAddressService.getMyAddresses(userId));
    }

    @GetMapping("/default")
    public Result<UserAddress> getDefaultAddress(@RequestAttribute("userId") Long userId) {
        UserAddress addr = userAddressService.getDefaultAddress(userId);
        return addr != null ? Result.success(addr) : Result.fail("暂无默认地址");
    }

    @PostMapping("/add")
    public Result<?> addAddress(@RequestBody UserAddress address,
                                @RequestAttribute("userId") Long userId) {
        address.setUserId(userId);
        boolean success = userAddressService.addAddress(address);
        return success ? Result.success(null, "添加成功") : Result.fail("添加失败");
    }

    @PutMapping("/update")
    public Result<?> updateAddress(@RequestBody UserAddress address,
                                   @RequestAttribute("userId") Long userId) {
        address.setUserId(userId);
        boolean success = userAddressService.updateAddress(address);
        return success ? Result.success(null, "更新成功") : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAddress(@PathVariable Long id,
                                   @RequestAttribute("userId") Long userId) {
        boolean success = userAddressService.deleteAddress(id, userId);
        return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefault(@PathVariable Long id,
                                @RequestAttribute("userId") Long userId) {
        boolean success = userAddressService.setDefault(id, userId);
        return success ? Result.success(null, "设置成功") : Result.fail("设置失败");
    }
}
