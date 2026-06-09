package com.carshare.service;

import com.carshare.entity.Refund;
import java.util.List;
import java.util.Map;

public interface RefundService {
    boolean applyRefund(Refund refund);
    boolean reviewRefund(Long refundId, Long reviewerId, Integer status, String rejectReason);
    boolean autoRefundForFailedCar(Long carId);
    Map<String, Object> getMyRefunds(Long userId, Integer page, Integer pageSize);
    Map<String, Object> getCarRefunds(Long carId);
    List<Refund> getPendingRefunds(Long carId, Long ownerId);
}
