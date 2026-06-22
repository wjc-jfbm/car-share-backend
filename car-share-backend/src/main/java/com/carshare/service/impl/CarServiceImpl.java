package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.enums.CarStatus;
import com.carshare.entity.Car;
import com.carshare.entity.CarMember;
import com.carshare.entity.Goods;
import com.carshare.entity.User;
import com.carshare.mapper.CarMapper;
import com.carshare.mapper.CarMemberMapper;
import com.carshare.mapper.GoodsMapper;
import com.carshare.mapper.UserMapper;
import com.carshare.common.utils.PageResult;
import com.carshare.service.BlacklistService;
import com.carshare.service.CarService;
import com.carshare.service.MatchService;
import com.carshare.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {

    private static final Logger log = LoggerFactory.getLogger(CarServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarMemberMapper carMemberMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MatchService matchService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BlacklistService blacklistService;

    @Override
    public Map<String, Object> getCarList(Integer page, Integer pageSize, Integer status, String keyword,
                                           BigDecimal priceMin, BigDecimal priceMax, String sortBy) {
        Page<Car> pageObj = new Page<>(page, pageSize);
        var carPage = carMapper.selectCarPage(pageObj, status, keyword, priceMin, priceMax, sortBy);
        return PageResult.of(carPage);
    }

    @Override
    public Car getCarDetail(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car != null) {
            User owner = userMapper.selectById(car.getUserId());
            if (owner != null) {
                car.setUserNickname(owner.getNickname());
                car.setUserCredit(owner.getCreditScore());
            }

            LambdaQueryWrapper<CarMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CarMember::getCarId, carId).orderByAsc(CarMember::getIsOwner).orderByAsc(CarMember::getJoinTime);
            List<CarMember> members = carMemberMapper.selectList(wrapper);

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

            car.setMembers(members);

            if (car.getGoodsId() != null) {
                Goods goods = goodsMapper.selectById(car.getGoodsId());
                car.setGoods(goods);
            }
        }
        return car;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "carList", allEntries = true),
            @CacheEvict(value = "carDetail", allEntries = true)
    })
    public Long createCar(Long userId, Car car) {
        car.setUserId(userId);
        car.setCurrentCount(0);
        car.setStatus(CarStatus.RECRUITING.getCode());
        try {
            car.setSuccessRate(matchService.calculateSuccessRate(car));
        } catch (Exception e) {
            car.setSuccessRate(BigDecimal.ZERO);
        }

        if (car.getGoodsId() == null && car.getGoodsName() != null && !car.getGoodsName().isEmpty()) {
            Goods goods = new Goods();
            goods.setName(car.getGoodsName());
            goods.setType("专辑");
            if (car.getGoodsImage() != null && !car.getGoodsImage().isEmpty()) {
                goods.setImageUrl(car.getGoodsImage());
            }
            goods.setMarketPrice(car.getPriceTotal());
            goods.setStatus(1);
            goodsMapper.insert(goods);
            car.setGoodsId(goods.getId());
        }

        // 如果前端传了versions/cards，更新到商品
        if (car.getGoodsId() != null) {
            Goods existingGoods = goodsMapper.selectById(car.getGoodsId());
            if (existingGoods != null) {
                boolean needUpdate = false;
                // 从请求参数中获取versions和cards（通过tags字段临时传递或直接设置到goods）
                if (car.getTags() != null && car.getTags().startsWith("{")) {
                    try {
                        java.util.Map<String, Object> extra = OBJECT_MAPPER.readValue(car.getTags(), java.util.Map.class);
                        if (extra.containsKey("versions")) {
                            existingGoods.setVersions(OBJECT_MAPPER.writeValueAsString(extra.get("versions")));
                            needUpdate = true;
                        }
                        if (extra.containsKey("cards")) {
                            existingGoods.setCards(OBJECT_MAPPER.writeValueAsString(extra.get("cards")));
                            needUpdate = true;
                        }
                        if (needUpdate) {
                            car.setTags(null); // 清除临时数据
                        }
                    } catch (Exception ignored) {}
                }
                if (needUpdate) {
                    goodsMapper.updateById(existingGoods);
                }
            }
        }

        carMapper.insert(car);

        CarMember ownerMember = new CarMember();
        ownerMember.setCarId(car.getId());
        ownerMember.setUserId(userId);
        ownerMember.setIsOwner(1);
        ownerMember.setAmount(car.getPricePer());
        ownerMember.setClaimStatus(0);
        ownerMember.setPayStatus(1);
        ownerMember.setEvidenceStatus(1);
        ownerMember.setDistributionStatus(0);
        ownerMember.setJoinTime(LocalDateTime.now());
        ownerMember.setCreatedAt(LocalDateTime.now());
        carMemberMapper.insert(ownerMember);

        car.setCurrentCount(1);
        carMapper.updateById(car);

        return car.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = "carList", allEntries = true)
    public boolean joinCar(Long carId, Long userId, CarMember member) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            return false;
        }
        if (car.getStatus() != CarStatus.RECRUITING.getCode() && car.getStatus() != CarStatus.CLOSED.getCode()) {
            return false;
        }
        if (car.getDeadline() != null && car.getDeadline().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (car.getCurrentCount() >= car.getTotalCount()) {
            return false;
        }

        LambdaQueryWrapper<CarMember> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(CarMember::getCarId, carId).eq(CarMember::getUserId, userId);
        if (carMemberMapper.selectCount(existWrapper) > 0) {
            return false;
        }

        if (car.getIsRestricted() != null && car.getIsRestricted() == 1 && car.getMinCreditScore() != null) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getCreditScore() < car.getMinCreditScore()) {
                return false;
            }
        }

        // 黑名单校验：车主是否拉黑了该用户（表不存在时跳过）
        try {
            if (blacklistService.isBlocked(car.getUserId(), userId)) {
                return false;
            }
        } catch (Exception e) {
            log.warn("黑名单校验跳过（表可能不存在）: {}", e.getMessage());
        }

        member.setCarId(carId);
        member.setUserId(userId);
        member.setAmount(car.getPricePer());
        member.setClaimStatus(0);
        member.setPayStatus(0);
        member.setEvidenceStatus(0);
        member.setDistributionStatus(0);
        member.setIsOwner(0);
        member.setJoinTime(LocalDateTime.now());
        member.setCreatedAt(LocalDateTime.now());
        carMemberMapper.insert(member);

        car.setCurrentCount(car.getCurrentCount() + 1);
        carMapper.updateById(car);

        User joinUser = userMapper.selectById(userId);
        String joinNickname = joinUser != null ? joinUser.getNickname() : "新用户";
        notificationService.sendNotification(
                car.getUserId(), carId, "新成员加入",
                joinNickname + " 加入了您的拼车「" + car.getTitle() + "」", 1);

        if (car.getCurrentCount() >= car.getTotalCount()) {
            car.setStatus(CarStatus.CLOSED.getCode());
            car.setClosedAt(LocalDateTime.now());
            carMapper.updateById(car);

            // 满员后自动触发智能分配
            try {
                matchService.smartDistribute(carId);
            } catch (Exception e) {
                log.warn("自动分配失败: {}", e.getMessage());
            }

            notificationService.sendToCarMembers(
                    carId, null, "🎉 成团成功 + 分配完成",
                    "拼车「" + car.getTitle() + "」已满员成团，系统已智能分配版本/小卡，请查看你的分配结果", 2);
            notificationService.sendNotification(
                    car.getUserId(), carId, "🎉 成团成功",
                    "您发起的拼车「" + car.getTitle() + "」已满员成团，系统已完成智能分配，共" + car.getCurrentCount() + "人参与", 2);
        }

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "carList", allEntries = true)
    public boolean closeCar(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }
        car.setStatus(CarStatus.CLOSED.getCode());
        car.setClosedAt(LocalDateTime.now());
        boolean result = carMapper.updateById(car) > 0;

        if (result) {
            notificationService.sendToCarMembers(
                    carId, null, "成团成功",
                    "拼车「" + car.getTitle() + "」已成功成团，请尽快完成付款并上传凭证", 2);
            notificationService.sendNotification(
                    userId, carId, "成团成功",
                    "您发起的拼车「" + car.getTitle() + "」已成功成团，当前共" + car.getCurrentCount() + "人参与", 2);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean claimItem(Long carId, Long userId, String claimedVersion, String claimedCard) {
        LambdaQueryWrapper<CarMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarMember::getCarId, carId).eq(CarMember::getUserId, userId);
        CarMember member = carMemberMapper.selectOne(wrapper);
        if (member == null) {
            return false;
        }

        member.setClaimedVersion(claimedVersion);
        member.setClaimedCard(claimedCard);
        member.setClaimStatus(1);
        return carMemberMapper.updateById(member) > 0;
    }

    @Override
    @Transactional
    public boolean uploadPayEvidence(Long carMemberId, Long userId, String evidenceUrl) {
        CarMember member = carMemberMapper.selectById(carMemberId);
        if (member == null || !member.getUserId().equals(userId)) {
            return false;
        }

        member.setEvidenceUrl(evidenceUrl);
        member.setEvidenceStatus(0);
        return carMemberMapper.updateById(member) > 0;
    }

    @Override
    @Transactional
    public boolean reviewPayEvidence(Long carMemberId, Long userId, Integer status, String rejectReason) {
        CarMember member = carMemberMapper.selectById(carMemberId);
        if (member == null) {
            return false;
        }

        Car car = carMapper.selectById(member.getCarId());
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }

        member.setEvidenceStatus(status);
        if (status == 1) {
            member.setPayStatus(1);
        } else if (status == 2) {
            member.setEvidenceRejectReason(rejectReason);
        }
        return carMemberMapper.updateById(member) > 0;
    }

    @Override
    @Transactional
    public Map<String, Object> distribute(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(userId)) {
            Map<String, Object> errMap = new HashMap<>();
            errMap.put("error", "无权操作");
            return errMap;
        }

        Map<String, Integer> distributeResult = matchService.smartDistribute(carId);
        Map<String, Object> result = new HashMap<>(distributeResult);

        notificationService.sendToCarMembers(
                carId, userId, "分配结果已出",
                "拼车「" + car.getTitle() + "」已完成智能分配，请查看您的分配结果", 3);

        return result;
    }

    @Override
    public Map<String, Object> getMyCars(Long userId, Integer page, Integer pageSize) {
        Page<Car> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getUserId, userId).orderByDesc(Car::getCreatedAt);
        return PageResult.of(carMapper.selectPage(pageObj, wrapper));
    }

    @Override
    public Map<String, Object> getMyAllCars(Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getUserId, userId);
        List<CarMember> myMembers = carMemberMapper.selectList(memberWrapper);

        List<Long> carIds = myMembers.stream().map(CarMember::getCarId).distinct().toList();
        if (carIds.isEmpty()) {
            return PageResult.empty(page, pageSize);
        }

        Page<Car> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
        carWrapper.in(Car::getId, carIds).orderByDesc(Car::getCreatedAt);
        return PageResult.of(carMapper.selectPage(pageObj, carWrapper));
    }

    @Override
    public Map<String, Object> getHistoryCars(Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getUserId, userId);
        List<CarMember> myMembers = carMemberMapper.selectList(memberWrapper);

        List<Long> carIds = myMembers.stream().map(CarMember::getCarId).distinct().toList();
        if (carIds.isEmpty()) {
            return PageResult.empty(page, pageSize);
        }

        Page<Car> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
        carWrapper.in(Car::getId, carIds)
                 .notIn(Car::getStatus, CarStatus.RECRUITING.getCode())
                 .orderByDesc(Car::getCreatedAt);
        return PageResult.of(carMapper.selectPage(pageObj, carWrapper));
    }

    @Override
    public Map<String, Object> getMyJoinedCars(Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getUserId, userId).eq(CarMember::getIsOwner, 0);
        List<CarMember> myMembers = carMemberMapper.selectList(memberWrapper);

        List<Long> carIds = myMembers.stream().map(CarMember::getCarId).toList();
        if (carIds.isEmpty()) {
            return PageResult.empty(page, pageSize);
        }

        Page<Car> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
        carWrapper.in(Car::getId, carIds).orderByDesc(Car::getCreatedAt);
        return PageResult.of(carMapper.selectPage(pageObj, carWrapper));
    }

    @Override
    @Transactional
    public boolean completeCar(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }
        if (car.getStatus() != CarStatus.SHIPPED.getCode()) {
            return false;
        }
        car.setStatus(CarStatus.COMPLETED.getCode());
        car.setCompletedAt(LocalDateTime.now());
        return carMapper.updateById(car) > 0;
    }

    @Override
    @Transactional
    public boolean cancelCar(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !car.getUserId().equals(userId)) {
            return false;
        }
        if (car.getStatus() != CarStatus.RECRUITING.getCode()) {
            return false;
        }
        car.setStatus(CarStatus.CANCELLED.getCode());
        return carMapper.updateById(car) > 0;
    }

    @Override
    public List<Car> adminList(Car car) {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        if (car.getStatus() != null) {
            wrapper.eq(Car::getStatus, car.getStatus());
        }
        if (car.getTitle() != null && !car.getTitle().isEmpty()) {
            wrapper.like(Car::getTitle, car.getTitle());
        }
        wrapper.orderByDesc(Car::getCreatedAt);
        return carMapper.selectList(wrapper);
    }

    @Override
    public IPage<Car> adminListPage(Page<Car> page, Car car) {
        return carMapper.selectAdminCarPage(page, car);
    }
 
    @Override
    @Transactional
    @CacheEvict(value = "carList", allEntries = true)
    public boolean updateCar(Car car, Long userId) {
        Car existing = carMapper.selectById(car.getId());
        if (existing == null) return false;
        if (existing.getStatus() != CarStatus.RECRUITING.getCode()) return false;
        if (!existing.getUserId().equals(userId)) return false;
        if (car.getTitle() != null) existing.setTitle(car.getTitle());
        if (car.getDescription() != null) existing.setDescription(car.getDescription());
        if (car.getDeadline() != null) existing.setDeadline(car.getDeadline());
        if (car.getPriceTotal() != null) {
            existing.setPriceTotal(car.getPriceTotal());
            existing.setPricePer(car.getPriceTotal().divide(
                    BigDecimal.valueOf(existing.getTotalCount() != null && existing.getTotalCount() > 0 ? existing.getTotalCount() : 1),
                    RoundingMode.HALF_UP));
        }
        return carMapper.updateById(existing) > 0;
    }

    @Override
    public List<Map<String, Object>> exportMyCars(Long userId) {
        LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CarMember::getUserId, userId);
        List<CarMember> myMembers = carMemberMapper.selectList(memberWrapper);

        List<Long> carIds = myMembers.stream().map(CarMember::getCarId).distinct().toList();
        if (carIds.isEmpty()) return Collections.emptyList();

        List<Car> cars = carMapper.selectBatchIds(carIds);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Car car : cars) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("title", car.getTitle());
            row.put("goods", car.getGoodsName());
            row.put("status", CarStatus.getLabel(car.getStatus()));
            row.put("totalPrice", car.getPriceTotal());
            row.put("perPrice", car.getPricePer());
            row.put("members", car.getCurrentCount() + "/" + car.getTotalCount());
            row.put("createdAt", car.getCreatedAt() != null ? car.getCreatedAt().toString() : "");
            result.add(row);
        }
        return result;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "carList", allEntries = true),
            @CacheEvict(value = "carDetail", allEntries = true)
    })
    public boolean deleteCars(Long[] carIds) {
        for (Long carId : carIds) {
            carMemberMapper.delete(new LambdaQueryWrapper<CarMember>().eq(CarMember::getCarId, carId));
            carMapper.deleteById(carId);
        }
        return true;
    }

    /**
     * 定时任务：每5分钟自动关闭已过期的招募中拼车
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "carList", allEntries = true),
            @CacheEvict(value = "carDetail", allEntries = true)
    })
    public void autoCloseExpiredCarsTask() {
        try {
            LocalDateTime now = LocalDateTime.now();
            carMapper.autoCloseExpiredCars(now);
            log.info("自动关闭过期拼车检查完成");
        } catch (Exception e) {
            log.error("自动关闭过期拼车时出错", e);
        }
    }

    /**
     * 定时任务：每小时检查并发送到期/付款提醒
     * 1. 截止时间即将到达（24小时内）→ 提醒车主
     * 2. 拼车已满员但成员未付款 → 提醒未付款成员
     */
    @Scheduled(fixedRate = 3600000)
    public void autoSendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadlineThreshold = now.plusHours(24);

            // 1. 查找24小时内到期的招募中拼车，提醒车主
            LambdaQueryWrapper<Car> deadlineWrapper = new LambdaQueryWrapper<>();
            deadlineWrapper.eq(Car::getStatus, CarStatus.RECRUITING.getCode())
                    .isNotNull(Car::getDeadline)
                    .le(Car::getDeadline, deadlineThreshold)
                    .ge(Car::getDeadline, now);
            List<Car> expiringCars = carMapper.selectList(deadlineWrapper);

            for (Car car : expiringCars) {
                try {
                    notificationService.sendNotification(
                            car.getUserId(), car.getId(), "⏰ 拼车即将截止",
                            "您的拼车「" + car.getTitle() + "」将在 " +
                                    java.time.Duration.between(now, car.getDeadline()).toHours() + " 小时后截止，当前共" +
                                    car.getCurrentCount() + "/" + car.getTotalCount() + "人参与", 1);
                } catch (Exception ignored) {}
            }

            // 2. 查找满员/已截止但未完全付款的拼车 → 提醒未付款成员
            LambdaQueryWrapper<Car> unpaidWrapper = new LambdaQueryWrapper<>();
            unpaidWrapper.in(Car::getStatus, CarStatus.CLOSED.getCode());
            List<Car> closedCars = carMapper.selectList(unpaidWrapper);

            for (Car car : closedCars) {
                try {
                    LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
                    memberWrapper.eq(CarMember::getCarId, car.getId())
                            .eq(CarMember::getPayStatus, 0)
                            .ne(CarMember::getIsOwner, 1);
                    List<CarMember> unpaidMembers = carMemberMapper.selectList(memberWrapper);

                    for (CarMember member : unpaidMembers) {
                        notificationService.sendNotification(
                                member.getUserId(), car.getId(), "💳 付款提醒",
                                "拼车「" + car.getTitle() + "」已截止，请尽快完成付款并上传凭证", 4);
                    }
                } catch (Exception ignored) {}
            }

            if (!expiringCars.isEmpty() || !closedCars.isEmpty()) {
                log.info("自动提醒完成：{} 个即将截止, {} 个有未付款成员",
                        expiringCars.size(), closedCars.size());
            }
        } catch (Exception e) {
            log.error("自动发送提醒时出错", e);
        }
    }
}
