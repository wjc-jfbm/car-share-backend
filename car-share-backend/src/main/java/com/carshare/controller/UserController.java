package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.User;
import com.carshare.entity.UserPreference;
import com.carshare.service.UserPreferenceService;
import com.carshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        Map<String, Object> data = userService.wxLogin(code);
        return Result.success(data, "登录成功");
    }

    @PostMapping("/phone-login")
    public Result<Map<String, Object>> accountLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || username.trim().isEmpty()) {
            return Result.fail("账号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        Map<String, Object> data = userService.accountLogin(username, password);
        return Result.success(data, "登录成功");
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");
        if (username == null || username.trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if (password == null || password.length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }
        Map<String, Object> data = userService.accountRegister(username, password, nickname);
        return Result.success(data, "注册成功");
    }

    @PostMapping("/wx-phone-login")
    public Result<Map<String, Object>> wxPhoneLogin(@RequestBody Map<String, String> params) {
        String loginCode = params.get("loginCode");
        String phoneCode = params.get("phoneCode");
        if (loginCode == null || loginCode.trim().isEmpty()) {
            return Result.fail("登录code不能为空");
        }
        if (phoneCode == null || phoneCode.trim().isEmpty()) {
            return Result.fail("手机号授权code不能为空");
        }
        Map<String, Object> data = userService.wxPhoneLogin(loginCode, phoneCode);
        return Result.success(data, "登录成功");
    }

    @GetMapping("/profile")
    public Result<?> getProfile(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestAttribute("userId") Long userId,
                                   @RequestBody Map<String, String> params) {
        var user = new User();
        user.setNickname(params.get("nickname"));
        user.setPhone(params.get("phone"));
        user.setAvatar(params.get("avatar"));
        user.setRealName(params.get("realName"));
        userService.updateUserProfile(userId, user);
        return Result.success(null, "更新成功");
    }

    @GetMapping("/credit")
    public Result<?> getCredit(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getUserCredit(userId));
    }

    @GetMapping("/preference")
    public Result<?> getPreference(@RequestAttribute("userId") Long userId) {
        UserPreference pref = userPreferenceService.getByUserId(userId);
        return Result.success(pref);
    }

    @PostMapping("/preference")
    public Result<?> savePreference(@RequestAttribute("userId") Long userId,
                                    @RequestBody UserPreference preference) {
        boolean success = userPreferenceService.saveOrUpdate(userId, preference);
        return success ? Result.success(null, "保存成功") : Result.fail("保存失败");
    }
}
