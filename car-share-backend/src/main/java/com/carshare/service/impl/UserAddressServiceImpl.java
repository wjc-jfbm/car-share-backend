package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.entity.UserAddress;
import com.carshare.mapper.UserAddressMapper;
import com.carshare.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> getMyAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
               .eq(UserAddress::getStatus, 1)
               .orderByDesc(UserAddress::getIsDefault)
               .orderByDesc(UserAddress::getCreatedAt);
        return userAddressMapper.selectList(wrapper);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
               .eq(UserAddress::getIsDefault, 1)
               .eq(UserAddress::getStatus, 1);
        return userAddressMapper.selectOne(wrapper);
    }

    @Override
    public boolean addAddress(UserAddress address) {
        address.setStatus(1);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        return userAddressMapper.insert(address) > 0;
    }

    @Override
    public boolean updateAddress(UserAddress address) {
        UserAddress existing = userAddressMapper.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(address.getUserId())) return false;
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        address.setUpdatedAt(LocalDateTime.now());
        return userAddressMapper.updateById(address) > 0;
    }

    @Override
    public boolean deleteAddress(Long id, Long userId) {
        UserAddress existing = userAddressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) return false;
        existing.setStatus(0);
        existing.setUpdatedAt(LocalDateTime.now());
        return userAddressMapper.updateById(existing) > 0;
    }

    @Override
    public boolean setDefault(Long id, Long userId) {
        UserAddress existing = userAddressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) return false;
        clearDefault(userId);
        existing.setIsDefault(1);
        existing.setUpdatedAt(LocalDateTime.now());
        return userAddressMapper.updateById(existing) > 0;
    }

    private void clearDefault(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId).eq(UserAddress::getIsDefault, 1);
        List<UserAddress> defaults = userAddressMapper.selectList(wrapper);
        for (UserAddress addr : defaults) {
            addr.setIsDefault(0);
            userAddressMapper.updateById(addr);
        }
    }
}
