package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.entity.Blacklist;
import com.carshare.mapper.BlacklistMapper;
import com.carshare.service.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlacklistServiceImpl implements BlacklistService {

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Override
    public boolean addBlacklist(Long userId, Long blockedUserId, String reason) {
        if (isBlocked(userId, blockedUserId)) return true;
        Blacklist bl = new Blacklist();
        bl.setUserId(userId);
        bl.setBlockedUserId(blockedUserId);
        bl.setReason(reason);
        bl.setCreatedAt(LocalDateTime.now());
        return blacklistMapper.insert(bl) > 0;
    }

    @Override
    public boolean removeBlacklist(Long userId, Long blockedUserId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId).eq(Blacklist::getBlockedUserId, blockedUserId);
        return blacklistMapper.delete(wrapper) > 0;
    }

    @Override
    public List<Blacklist> getMyBlacklist(Long userId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId).orderByDesc(Blacklist::getCreatedAt);
        return blacklistMapper.selectList(wrapper);
    }

    @Override
    public boolean isBlocked(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId).eq(Blacklist::getBlockedUserId, targetUserId);
        return blacklistMapper.selectCount(wrapper) > 0;
    }
}
