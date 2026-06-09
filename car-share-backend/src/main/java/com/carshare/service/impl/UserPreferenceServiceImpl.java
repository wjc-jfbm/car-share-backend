package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.entity.UserPreference;
import com.carshare.mapper.UserPreferenceMapper;
import com.carshare.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @Override
    public UserPreference getByUserId(Long userId) {
        LambdaQueryWrapper<UserPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreference::getUserId, userId);
        return userPreferenceMapper.selectOne(wrapper);
    }

    @Override
    public boolean saveOrUpdate(Long userId, UserPreference preference) {
        LambdaQueryWrapper<UserPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPreference::getUserId, userId);
        UserPreference existing = userPreferenceMapper.selectOne(wrapper);

        if (existing == null) {
            preference.setUserId(userId);
            return userPreferenceMapper.insert(preference) > 0;
        } else {
            preference.setId(existing.getId());
            preference.setUserId(userId);
            return userPreferenceMapper.updateById(preference) > 0;
        }
    }
}
