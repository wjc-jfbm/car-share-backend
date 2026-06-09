package com.carshare.service;

import com.carshare.entity.Blacklist;
import java.util.List;

public interface BlacklistService {
    boolean addBlacklist(Long userId, Long blockedUserId, String reason);
    boolean removeBlacklist(Long userId, Long blockedUserId);
    List<Blacklist> getMyBlacklist(Long userId);
    boolean isBlocked(Long userId, Long targetUserId);
}
