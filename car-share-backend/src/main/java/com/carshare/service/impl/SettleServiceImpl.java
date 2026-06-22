package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.enums.CarStatus;
import com.carshare.entity.*;
import com.carshare.mapper.*;
import com.carshare.common.utils.PageResult;
import com.carshare.service.SettleService;
import com.carshare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SettleServiceImpl implements SettleService {

    @Autowired
    private CarOrderMapper carOrderMapper;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarMemberMapper carMemberMapper;

    @Autowired
    private EvidenceMapper evidenceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Map<String, Object> getSettleInfo(Long carId, Long currentUserId) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            return null;
        }

        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getCarId, carId).orderByAsc(CarMember::getIsOwner).orderByAsc(CarMember::getJoinTime);
        List<CarMember> members = carMemberMapper.selectList(memberWrapper);

        // 批量加载用户信息，消除 N+1 查询
        if (!members.isEmpty()) {
            List<Long> userIds = members.stream().map(CarMember::getUserId).collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
            for (CarMember member : members) {
                User memberUser = userMap.get(member.getUserId());
                if (memberUser != null) {
                    member.setNickname(memberUser.getNickname());
                }
            }
        }

        long paidCount = members.stream().filter(m -> m.getPayStatus() != null && m.getPayStatus() == 1).count();
        long pendingEvidenceCount = members.stream().filter(m -> m.getEvidenceUrl() != null && m.getEvidenceStatus() != null && m.getEvidenceStatus() == 0).count();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CarMember m : members) {
            if (m.getAmount() != null && m.getPayStatus() != null && m.getPayStatus() == 1) {
                totalAmount = totalAmount.add(m.getAmount());
            }
        }

        boolean isOwner = currentUserId != null && car.getUserId().equals(currentUserId);

        Map<String, Object> info = new HashMap<>();
        info.put("car", car);
        info.put("members", members);
        info.put("is_owner", isOwner);
        info.put("total_amount", totalAmount);
        info.put("member_count", members.size());
        info.put("paid_count", paidCount);
        info.put("pending_count", pendingEvidenceCount);
        return info;
    }

    @Override
    public List<CarOrder> getOrdersByCarId(Long carId) {
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarOrder::getCarId, carId).orderByDesc(CarOrder::getCreatedAt);
        return carOrderMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean createOrder(CarOrder order) {
        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        order.setSettleStatus(0);
        return carOrderMapper.insert(order) > 0;
    }

    @Override
    @Transactional
    public boolean uploadEvidence(Long carMemberId, Long carId, Long userId, Integer type, String imageUrl, String remark) {
        Evidence evidence = new Evidence();
        evidence.setCarMemberId(carMemberId);
        evidence.setCarId(carId);
        evidence.setUserId(userId);
        evidence.setType(type);
        evidence.setImageUrl(imageUrl);
        evidence.setRemark(remark);
        evidence.setStatus(0);
        evidenceMapper.insert(evidence);

        // 同时更新car_member的凭证状态
        CarMember member = carMemberMapper.selectById(carMemberId);
        if (member != null) {
            member.setEvidenceUrl(imageUrl);
            member.setEvidenceStatus(0);
            carMemberMapper.updateById(member);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean reviewEvidence(Long evidenceId, Long reviewerId, Integer status, String rejectReason) {
        Evidence evidence = evidenceMapper.selectById(evidenceId);
        if (evidence == null) {
            return false;
        }

        Car car = carMapper.selectById(evidence.getCarId());
        if (car == null || !car.getUserId().equals(reviewerId)) {
            return false;
        }

        evidence.setStatus(status);
        evidence.setReviewedBy(reviewerId);
        evidence.setReviewedAt(LocalDateTime.now());
        if (status == 2 && rejectReason != null) {
            evidence.setRemark(rejectReason);
        }
        evidenceMapper.updateById(evidence);

        if (status == 1) {
            CarMember member = carMemberMapper.selectById(evidence.getCarMemberId());
            if (member != null) {
                member.setEvidenceStatus(1);
                if (evidence.getType() != null && evidence.getType() == 0) {
                    member.setPayStatus(1);
                    member.setDepositPaid(member.getAmount());
                } else if (evidence.getType() != null && evidence.getType() == 1) {
                    member.setPayStatus(2);
                    member.setBalancePaid(member.getAmount());
                }
                carMemberMapper.updateById(member);
            }
        } else if (status == 2) {
            CarMember member = carMemberMapper.selectById(evidence.getCarMemberId());
            if (member != null) {
                member.setEvidenceStatus(2);
                member.setEvidenceRejectReason(rejectReason);
                carMemberMapper.updateById(member);
            }
        }

        String resultText = status == 1 ? "已通过" : "已被驳回";
        String carTitle = car != null ? car.getTitle() : "";
        notificationService.sendNotification(
                evidence.getUserId(), evidence.getCarId(), "凭证审核结果",
                "您在拼车「" + carTitle + "」中上传的付款凭证" + resultText, 4);

        return true;
    }

    @Override
    public List<Evidence> getEvidencesByCarId(Long carId) {
        LambdaQueryWrapper<Evidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evidence::getCarId, carId).orderByDesc(Evidence::getCreatedAt);
        return evidenceMapper.selectList(wrapper);
    }

    @Override
    public List<Evidence> getEvidencesByUserId(Long userId) {
        LambdaQueryWrapper<Evidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evidence::getUserId, userId).orderByDesc(Evidence::getCreatedAt);
        return evidenceMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean settleOrder(Long orderId, Long userId) {
        CarOrder order = carOrderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        Car car = carMapper.selectById(order.getCarId());
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }

        order.setSettleStatus(1);
        order.setStatus(2);
        boolean result = carOrderMapper.updateById(order) > 0;

        if (result) {
            String carTitle = car != null ? car.getTitle() : "";
            notificationService.sendNotification(
                    order.getUserId(), order.getCarId(), "结算完成",
                    "拼车「" + carTitle + "」的订单已结算完成", 5);
        }

        return result;
    }

    @Override
    public Map<String, Object> getMyOrders(Long userId, Integer page, Integer pageSize) {
        Page<CarOrder> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarOrder::getUserId, userId).orderByDesc(CarOrder::getCreatedAt);
        return PageResult.of(carOrderMapper.selectPage(pageObj, wrapper));
    }

    @Override
    @Transactional
    public boolean confirmSettle(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }
        if (car.getStatus() != CarStatus.CLOSED.getCode()) {
            return false;
        }

        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getCarId, carId);
        List<CarMember> members = carMemberMapper.selectList(memberWrapper);

        boolean allPaid = members.stream()
                .filter(m -> m.getIsOwner() == null || m.getIsOwner() != 1)
                .allMatch(m -> m.getPayStatus() != null && (m.getPayStatus() == 1 || m.getPayStatus() == 2));
        if (!allPaid) {
            return false;
        }

        for (CarMember member : members) {
            if (member.getIsOwner() != null && member.getIsOwner() == 1) {
                continue; // 团长不需要创建订单
            }
            CarOrder order = new CarOrder();
            order.setCarId(carId);
            order.setCarMemberId(member.getId());
            order.setUserId(member.getUserId());
            order.setAmount(member.getAmount());
            createOrder(order);
        }

        car.setStatus(CarStatus.SETTLED.getCode());
        carMapper.updateById(car);

        notificationService.sendToCarMembers(
                carId, userId, "结算确认",
                "拼车「" + car.getTitle() + "」已确认结算，请等待车主发货", 5);

        return true;
    }

    @Override
    public Map<String, Object> getShippingInfo(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(userId)) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("carId", carId);
        result.put("title", car.getTitle());
        result.put("status", car.getStatus());
        result.put("goodsName", car.getGoodsName());

        if (car.getStatus() >= 3) {
            LambdaQueryWrapper<Logistics> logiWrapper = new LambdaQueryWrapper<>();
            logiWrapper.eq(Logistics::getCarId, carId).orderByDesc(Logistics::getCreatedAt);
            List<Logistics> logistics = logisticsMapper.selectList(logiWrapper);
            result.put("logistics", logistics);
        }

        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getCarId, carId).eq(CarMember::getIsOwner, 0);
        List<CarMember> members = carMemberMapper.selectList(memberWrapper);
        if (!members.isEmpty()) {
            List<Long> userIds = members.stream().map(CarMember::getUserId).collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
            for (CarMember member : members) {
                User memberUser = userMap.get(member.getUserId());
                if (memberUser != null) {
                    member.setNickname(memberUser.getNickname());
                }
            }
        }
        result.put("members", members);

        return result;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + random;
    }
}
