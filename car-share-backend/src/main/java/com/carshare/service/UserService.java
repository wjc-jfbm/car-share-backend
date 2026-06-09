package com.carshare.service;

import com.carshare.entity.User;

import java.util.Map;

public interface UserService {
    Map<String, Object> wxLogin(String code);
    Map<String, Object> accountLogin(String username, String password);
    Map<String, Object> accountRegister(String username, String password, String nickname);
    Map<String, Object> wxPhoneLogin(String loginCode, String phoneCode);
    User getUserProfile(Long userId);
    boolean updateUserProfile(Long userId, User user);
    Map<String, Object> getUserCredit(Long userId);
}