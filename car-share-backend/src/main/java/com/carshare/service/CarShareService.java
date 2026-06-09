package com.carshare.service;

import com.carshare.entity.CarShareRecord;
import java.util.Map;

public interface CarShareService {
    CarShareRecord createShareRecord(Long carId, Long userId, String shareType);
    CarShareRecord getShareByCode(String shareCode);
    boolean recordInvite(String shareCode, Long inviteUserId);
    Map<String, Object> getShareStats(Long carId, Long userId);
}
