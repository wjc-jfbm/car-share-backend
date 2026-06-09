package com.carshare.service;

import com.carshare.entity.CarOrder;
import com.carshare.entity.Evidence;

import java.util.List;
import java.util.Map;

public interface SettleService {
    Map<String, Object> getSettleInfo(Long carId, Long currentUserId);
    List<CarOrder> getOrdersByCarId(Long carId);
    boolean createOrder(CarOrder order);
    boolean uploadEvidence(Long carMemberId, Long carId, Long userId, Integer type, String imageUrl, String remark);
    boolean reviewEvidence(Long evidenceId, Long reviewerId, Integer status, String rejectReason);
    List<Evidence> getEvidencesByCarId(Long carId);
    List<Evidence> getEvidencesByUserId(Long userId);
    boolean settleOrder(Long orderId, Long userId);
    Map<String, Object> getMyOrders(Long userId, Integer page, Integer pageSize);
    boolean confirmSettle(Long carId, Long userId);
    Map<String, Object> getShippingInfo(Long carId, Long userId);
}
