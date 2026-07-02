package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.entity.User;
import com.carshare.mapper.UserMapper;
import com.carshare.service.UserService;
import com.carshare.service.WeChatService;
import com.carshare.util.JwtUtil;
import com.carshare.common.utils.sign.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> accountLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, username).or().eq(User::getNickname, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        if (user.getPassword() == null || !user.getPassword().equals(Md5Utils.hash(password))) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("hasPhone", user.getPhone() != null && !user.getPhone().isEmpty());
        return result;
    }

    @Override
    public Map<String, Object> accountRegister(String username, String password, String nickname) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, username);
        User existing = userMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(username);
        user.setPassword(Md5Utils.hash(password));
        user.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname.trim() : "用户" + username.substring(username.length() - 4));
        user.setCreditScore(60);
        user.setCreditLevel(3);
        user.setStatus(1);
        userMapper.insert(user);

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("hasPhone", true);
        return result;
    }

    @Override
    public Map<String, Object> wxLogin(String code) {
        WeChatService.JsCode2SessionResult session = weChatService.jsCode2Session(code);
        String openid;

        if (session.isSuccess() && session.getOpenid() != null) {
            openid = session.getOpenid();
            log.info("wxLogin success, openid={}", openid);
        } else {
            openid = "wx_dev_" + code;
            log.warn("wxLogin wechat api failed (errcode={}, msg={}), fallback to dev mode with openid={}",
                    session.getErrcode(), session.getErrmsg(), openid);
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(session.getUnionid());
            user.setSessionKey(session.getSessionKey());
            user.setNickname("微信用户" + openid.substring(0, Math.min(4, openid.length())));
            user.setCreditScore(60);
            user.setCreditLevel(3);
            user.setStatus(1);
            userMapper.insert(user);
        } else {
            user.setSessionKey(session.getSessionKey());
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("hasPhone", user.getPhone() != null && !user.getPhone().isEmpty());
        return result;
    }

    @Override
    public Map<String, Object> wxPhoneLogin(String loginCode, String phoneCode) {
        WeChatService.JsCode2SessionResult session = weChatService.jsCode2Session(loginCode);
        String openid;

        if (session.isSuccess() && session.getOpenid() != null) {
            openid = session.getOpenid();
        } else {
            openid = "wx_dev_" + loginCode;
            log.warn("wxPhoneLogin wechat api failed, fallback to dev mode with openid={}", openid);
        }

        String phoneNumber = weChatService.getPhoneNumberFromCode(phoneCode);
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new RuntimeException("获取手机号失败，请使用手机号验证码登录");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, phoneNumber);
            user = userMapper.selectOne(phoneWrapper);

            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setUnionid(session.getUnionid());
                user.setPhone(phoneNumber);
                user.setNickname("用户" + phoneNumber.substring(phoneNumber.length() - 4));
                user.setCreditScore(60);
                user.setCreditLevel(3);
                user.setStatus(1);
                userMapper.insert(user);
            } else {
                user.setOpenid(openid);
                user.setUnionid(session.getUnionid());
                userMapper.updateById(user);
            }
        } else {
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                user.setPhone(phoneNumber);
            }
        }

        user.setSessionKey(session.getSessionKey());
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("hasPhone", true);
        return result;
    }

    @Override
    public User getUserProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public boolean updateUserProfile(Long userId, User user) {
        user.setId(userId);
        user.setOpenid(null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public Map<String, Object> getUserCredit(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("creditScore", user.getCreditScore());
        result.put("creditLevel", user.getCreditLevel());
        return result;
    }
}