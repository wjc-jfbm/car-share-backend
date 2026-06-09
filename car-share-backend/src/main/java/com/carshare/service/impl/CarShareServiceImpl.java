package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.entity.CarShareRecord;
import com.carshare.mapper.CarShareRecordMapper;
import com.carshare.service.CarShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CarShareServiceImpl implements CarShareService {

    @Autowired
    private CarShareRecordMapper carShareRecordMapper;

    @Override
    public CarShareRecord createShareRecord(Long carId, Long userId, String shareType) {
        CarShareRecord record = new CarShareRecord();
        record.setCarId(carId);
        record.setUserId(userId);
        record.setShareType(shareType);
        record.setShareCode(generateShareCode());
        record.setCreatedAt(LocalDateTime.now());
        carShareRecordMapper.insert(record);
        return record;
    }

    @Override
    public CarShareRecord getShareByCode(String shareCode) {
        LambdaQueryWrapper<CarShareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarShareRecord::getShareCode, shareCode);
        return carShareRecordMapper.selectOne(wrapper);
    }

    @Override
    public boolean recordInvite(String shareCode, Long inviteUserId) {
        CarShareRecord record = getShareByCode(shareCode);
        if (record == null) return false;
        record.setInviteUserId(inviteUserId);
        return carShareRecordMapper.updateById(record) > 0;
    }

    @Override
    public Map<String, Object> getShareStats(Long carId, Long userId) {
        LambdaQueryWrapper<CarShareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarShareRecord::getCarId, carId);
        if (userId != null) {
            wrapper.eq(CarShareRecord::getUserId, userId);
        }
        Long total = carShareRecordMapper.selectCount(wrapper);

        LambdaQueryWrapper<CarShareRecord> inviteWrapper = new LambdaQueryWrapper<>();
        inviteWrapper.eq(CarShareRecord::getCarId, carId).isNotNull(CarShareRecord::getInviteUserId);
        Long invited = carShareRecordMapper.selectCount(inviteWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShares", total);
        stats.put("totalInvited", invited);
        return stats;
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
