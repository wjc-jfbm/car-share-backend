package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.enums.CarStatus;
import com.carshare.entity.*;
import com.carshare.mapper.*;
import com.carshare.service.NotificationService;
import com.carshare.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundMapper refundMapper;
    @Autowired
    private CarMapper carMapper;
    @Autowired
    private CarMemberMapper carMemberMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public boolean applyRefund(Refund refund) {
        CarMember member = carMemberMapper.selectById(refund.getCarMemberId());
        if (member == null || !member.getUserId().equals(refund.getUserId())) {
            return false;
        }
        Car car = carMapper.selectById(refund.getCarId());
        if (car == null) {
            return false;
        }
        refund.setStatus(0);
        refund.setAmount(member.getAmount());
        refund.setCreatedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        refundMapper.insert(refund);

        notificationService.sendNotification(
            car.getUserId(), car.getId(), "退款申请",
            "成员申请退出拼车「" + car.getTitle() + "」并请求退款", 6);
        return true;
    }

    @Override
    @Transactional
    public boolean reviewRefund(Long refundId, Long reviewerId, Integer status, String rejectReason) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) return false;
        Car car = carMapper.selectById(refund.getCarId());
        if (car == null || !car.getUserId().equals(reviewerId)) return false;

        refund.setStatus(status);
        refund.setReviewedBy(reviewerId);
        refund.setReviewedAt(LocalDateTime.now());
        if (status == 4 && rejectReason != null) {
            refund.setRejectReason(rejectReason);
        }
        if (status == 1) {
            refund.setStatus(2); // 审核通过直接进入退款中
            refund.setRefundedAt(LocalDateTime.now());
            // 更新成员状态
            CarMember member = carMemberMapper.selectById(refund.getCarMemberId());
            if (member != null) {
                member.setPayStatus(0);
                member.setEvidenceStatus(0);
                carMemberMapper.updateById(member);
            }
            // 更新拼车人数
            if (car.getCurrentCount() > 0) {
                car.setCurrentCount(car.getCurrentCount() - 1);
                if (car.getStatus() == CarStatus.CLOSED.getCode()) {
                    car.setStatus(CarStatus.RECRUITING.getCode()); // 回到招募中
                }
                carMapper.updateById(car);
            }
        }
        refund.setUpdatedAt(LocalDateTime.now());
        refundMapper.updateById(refund);

        String resultText = status == 2 ? "已通过，退款处理中" : "已被驳回";
        notificationService.sendNotification(
            refund.getUserId(), car.getId(), "退款审核结果",
            "您在拼车「" + car.getTitle() + "」中的退款申请" + resultText, 6);
        return true;
    }

    @Override
    @Transactional
    public boolean autoRefundForFailedCar(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car == null) return false;

        LambdaQueryWrapper<CarMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarMember::getCarId, carId).eq(CarMember::getIsOwner, 0);
        List<CarMember> members = carMemberMapper.selectList(wrapper);

        for (CarMember member : members) {
            Refund refund = new Refund();
            refund.setCarId(carId);
            refund.setCarMemberId(member.getId());
            refund.setUserId(member.getUserId());
            refund.setAmount(member.getAmount());
            refund.setType(2);
            refund.setReason("拼车未成团，自动退款");
            refund.setStatus(3); // 已到账
            refund.setRefundedAt(LocalDateTime.now());
            refund.setCreatedAt(LocalDateTime.now());
            refund.setUpdatedAt(LocalDateTime.now());
            refundMapper.insert(refund);

            member.setPayStatus(0);
            carMemberMapper.updateById(member);

            notificationService.sendNotification(
                member.getUserId(), carId, "拼车未成团退款",
                "拼车「" + car.getTitle() + "」未成团，已自动退款", 6);
        }
        car.setStatus(CarStatus.COMPLETED.getCode()); // 已关闭
        car.setClosedAt(LocalDateTime.now());
        carMapper.updateById(car);
        return true;
    }

    @Override
    public Map<String, Object> getMyRefunds(Long userId, Integer page, Integer pageSize) {
        Page<Refund> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getUserId, userId).orderByDesc(Refund::getCreatedAt);
        Page<Refund> result = refundMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    @Override
    public Map<String, Object> getCarRefunds(Long carId) {
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getCarId, carId).orderByDesc(Refund::getCreatedAt);
        List<Refund> list = refundMapper.selectList(wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", list.size());
        return map;
    }

    @Override
    public List<Refund> getPendingRefunds(Long carId, Long ownerId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(ownerId)) return List.of();

        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getCarId, carId).eq(Refund::getStatus, 0).orderByDesc(Refund::getCreatedAt);
        return refundMapper.selectList(wrapper);
    }
}
