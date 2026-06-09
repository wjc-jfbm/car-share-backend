package com.carshare.service;

import com.carshare.entity.UserPreference;

public interface UserPreferenceService {
    UserPreference getByUserId(Long userId);
    boolean saveOrUpdate(Long userId, UserPreference preference);
}
