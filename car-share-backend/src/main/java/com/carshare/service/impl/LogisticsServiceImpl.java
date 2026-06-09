package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Car;
import com.carshare.entity.CarMember;
import com.carshare.entity.Logistics;
import com.carshare.entity.User;
import com.carshare.mapper.CarMapper;
import com.carshare.mapper.CarMemberMapper;
import com.carshare.mapper.LogisticsMapper;
import com.carshare.mapper.UserMapper;
import com.carshare.common.utils.PageResult;
import com.carshare.service.LogisticsService;
import com.carshare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private CarMapper carMapper;
    @Autowired
    private CarMemberMapper carMemberMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public Logistics getByCarId(Long carId) {
        LambdaQueryWrapper<Logistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Logistics::getCarId, carId);
        return logisticsMapper.selectOne(wrapper);
    }

    @Override
    public Long createLogistics(Long userId, Logistics logistics) {
        Car car = carMapper.selectById(logistics.getCarId());
        if (car == null || !car.getUserId().equals(userId)) {
            return null;
        }
        if (car.getStatus() != 2) {
            return null;
        }
        logistics.setStatus(1);
        logistics.setCreatedAt(java.time.LocalDateTime.now());
        logistics.setUpdatedAt(java.time.LocalDateTime.now());
        logisticsMapper.insert(logistics);

        car.setStatus(3);
        carMapper.updateById(car);

        notificationService.sendToCarMembers(
                logistics.getCarId(), userId, "已发货",
                "拼车商品已发货，快递公司：" + logistics.getExpressCompany() + "，单号：" + logistics.getExpressNo(), 6);

        return logistics.getId();
    }

    @Override
    public boolean updateLogistics(Long id, Long userId, Logistics logistics) {
        Logistics existing = logisticsMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        Car car = carMapper.selectById(existing.getCarId());
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }
        logistics.setId(id);
        return logisticsMapper.updateById(logistics) > 0;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Long userId, Integer status) {
        Logistics logistics = logisticsMapper.selectById(id);
        if (logistics == null) {
            return false;
        }
        Car car = carMapper.selectById(logistics.getCarId());
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }
        logistics.setStatus(status);
        boolean result = logisticsMapper.updateById(logistics) > 0;

        if (result && status == 2) {
            completeDelivery(logistics, car);
        }

        return result;
    }

    @Override
    public Map<String, Object> getLogisticsList(Integer page, Integer pageSize) {
        Page<Logistics> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Logistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Logistics::getCreatedAt);
        Page<Logistics> result = logisticsMapper.selectPage(pageObj, wrapper);

        for (Logistics l : result.getRecords()) {
            fillLogisticsInfo(l);
        }

        return PageResult.of(result);
    }

    @Override
    public Map<String, Object> getLogisticsByCarId(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            return null;
        }

        LambdaQueryWrapper<Logistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Logistics::getCarId, carId).orderByDesc(Logistics::getCreatedAt);
        List<Logistics> logisticsList = logisticsMapper.selectList(wrapper);

        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getCarId, carId).eq(CarMember::getIsOwner, 0);
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

        Map<String, Object> result = new HashMap<>();
        result.put("car", car);
        result.put("logistics", logisticsList);
        result.put("list", logisticsList);
        result.put("members", members);
        return result;
    }

    @Override
    public Page<Logistics> adminListPage(Page<Logistics> page, Integer status, String expressNo) {
        LambdaQueryWrapper<Logistics> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Logistics::getStatus, status);
        }
        if (expressNo != null && !expressNo.isEmpty()) {
            wrapper.like(Logistics::getExpressNo, expressNo);
        }
        wrapper.orderByDesc(Logistics::getCreatedAt);
        Page<Logistics> result = logisticsMapper.selectPage(page, wrapper);

        for (Logistics l : result.getRecords()) {
            fillLogisticsInfo(l);
        }
        return result;
    }

    @Override
    public Map<String, Object> getAdminDetail(Long id) {
        Logistics logistics = logisticsMapper.selectById(id);
        if (logistics == null) {
            return null;
        }
        fillLogisticsInfo(logistics);

        Car car = carMapper.selectById(logistics.getCarId());
        if (car != null) {
            User owner = userMapper.selectById(car.getUserId());
            if (owner != null) {
                car.setUserNickname(owner.getNickname());
            }
        }

        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getCarId, logistics.getCarId()).eq(CarMember::getIsOwner, 0);
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

        Map<String, Object> detail = new HashMap<>();
        detail.put("logistics", logistics);
        detail.put("car", car);
        detail.put("members", members);
        return detail;
    }

    @Override
    @Transactional
    public boolean adminCreateLogistics(Logistics logistics) {
        Car car = carMapper.selectById(logistics.getCarId());
        if (car == null) {
            return false;
        }

        LambdaQueryWrapper<Logistics> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Logistics::getCarId, logistics.getCarId());
        if (logisticsMapper.selectCount(existWrapper) > 0) {
            return false;
        }

        logistics.setStatus(1);
        logistics.setCreatedAt(java.time.LocalDateTime.now());
        logistics.setUpdatedAt(java.time.LocalDateTime.now());
        logisticsMapper.insert(logistics);

        if (car.getStatus() != null && car.getStatus() == 2) {
            car.setStatus(3);
            carMapper.updateById(car);
        }

        notificationService.sendToCarMembers(
                logistics.getCarId(), null, "已发货",
                "拼车商品已发货，快递公司：" + logistics.getExpressCompany() + "，单号：" + logistics.getExpressNo(), 6);

        return true;
    }

    @Override
    @Transactional
    public boolean adminUpdateLogistics(Logistics logistics) {
        Logistics existing = logisticsMapper.selectById(logistics.getId());
        if (existing == null) {
            return false;
        }
        logistics.setUpdatedAt(java.time.LocalDateTime.now());
        return logisticsMapper.updateById(logistics) > 0;
    }

    @Override
    @Transactional
    public boolean adminUpdateStatus(Long id, Integer status) {
        Logistics logistics = logisticsMapper.selectById(id);
        if (logistics == null) {
            return false;
        }
        logistics.setStatus(status);
        logistics.setUpdatedAt(java.time.LocalDateTime.now());
        boolean result = logisticsMapper.updateById(logistics) > 0;

        if (result && status == 2) {
            Car car = carMapper.selectById(logistics.getCarId());
            completeDelivery(logistics, car);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean adminDeleteLogistics(Long[] ids) {
        for (Long id : ids) {
            logisticsMapper.deleteById(id);
        }
        return true;
    }

    private void fillLogisticsInfo(Logistics l) {
        if (l.getCarId() != null) {
            Car car = carMapper.selectById(l.getCarId());
            if (car != null) {
                l.setCarTitle(car.getTitle());
                User owner = userMapper.selectById(car.getUserId());
                if (owner != null) {
                    l.setOwnerName(owner.getNickname());
                    if (l.getSenderName() == null || l.getSenderName().isEmpty()) {
                        l.setSenderName(owner.getNickname());
                    }
                }
            }
        }
    }

    private void completeDelivery(Logistics logistics, Car car) {
        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getCarId, logistics.getCarId()).eq(CarMember::getIsOwner, 0);
        List<CarMember> members = carMemberMapper.selectList(memberWrapper);
        for (CarMember member : members) {
            member.setDistributionStatus(2);
            carMemberMapper.updateById(member);
        }

        if (car != null) {
            car.setStatus(4);
            car.setCompletedAt(java.time.LocalDateTime.now());
            carMapper.updateById(car);
        }

        notificationService.sendToCarMembers(
                logistics.getCarId(), null, "已签收",
                "拼车「" + (car != null ? car.getTitle() : "") + "」的商品已签收", 7);
    }
}
