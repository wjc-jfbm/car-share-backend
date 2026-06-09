package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.*;
import com.carshare.mapper.*;
import com.carshare.service.MatchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarMemberMapper carMemberMapper;

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double WEIGHT_MEMBER = 0.4;
    private static final double WEIGHT_VERSION = 0.3;
    private static final double WEIGHT_CARD = 0.3;

    private static final double SUCCESS_WEIGHT_HISTORY = 0.3;
    private static final double SUCCESS_WEIGHT_PARTICIPATION = 0.3;
    private static final double SUCCESS_WEIGHT_PRICE = 0.2;
    private static final double SUCCESS_WEIGHT_CREDIT = 0.2;

    @Override
    public BigDecimal calculateMatchScore(Long userId, Long carId) {
        UserPreference pref = userPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserPreference>().eq(UserPreference::getUserId, userId));
        Car car = carMapper.selectById(carId);
        if (pref == null || car == null) {
            return BigDecimal.ZERO;
        }
        return calculateMatchScore(pref, car);
    }

    @Override
    public BigDecimal calculateMatchScore(UserPreference preference, Car car) {
        try {
            double memberScore = calculateMemberMatch(preference, car);
            double versionScore = calculateVersionMatch(preference, car);
            double cardScore = calculateCardMatch(preference, car);

            double totalScore = memberScore * WEIGHT_MEMBER
                    + versionScore * WEIGHT_VERSION
                    + cardScore * WEIGHT_CARD;

            return BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private double calculateMemberMatch(UserPreference pref, Car car) {
        if (pref.getPreferredArtists() == null || car.getGoodsId() == null) {
            return 0.5;
        }
        try {
            Goods goods = goodsMapper.selectById(car.getGoodsId());
            if (goods == null || goods.getCards() == null) {
                return 0.5;
            }
            List<String> prefArtists = objectMapper.readValue(pref.getPreferredArtists(),
                    new TypeReference<List<String>>() {});
            List<String> carCards = objectMapper.readValue(goods.getCards(),
                    new TypeReference<List<String>>() {});

            if (prefArtists.isEmpty() || carCards.isEmpty()) {
                return 0.5;
            }

            long matchCount = carCards.stream()
                    .filter(card -> prefArtists.stream().anyMatch(card::contains))
                    .count();

            return (double) matchCount / Math.max(prefArtists.size(), carCards.size());
        } catch (Exception e) {
            return 0.5;
        }
    }

    private double calculateVersionMatch(UserPreference pref, Car car) {
        if (pref.getPreferredVersions() == null || car.getGoodsId() == null) {
            return 0.5;
        }
        try {
            Goods goods = goodsMapper.selectById(car.getGoodsId());
            if (goods == null || goods.getVersions() == null) {
                return 0.5;
            }
            List<String> prefVersions = objectMapper.readValue(pref.getPreferredVersions(),
                    new TypeReference<List<String>>() {});
            List<String> carVersions = objectMapper.readValue(goods.getVersions(),
                    new TypeReference<List<String>>() {});

            if (prefVersions.isEmpty() || carVersions.isEmpty()) {
                return 0.5;
            }

            long matchCount = carVersions.stream()
                    .filter(prefVersions::contains)
                    .count();

            return (double) matchCount / Math.max(prefVersions.size(), carVersions.size());
        } catch (Exception e) {
            return 0.5;
        }
    }

    private double calculateCardMatch(UserPreference pref, Car car) {
        if (pref.getPreferredCards() == null || car.getGoodsId() == null) {
            return 0.5;
        }
        try {
            Goods goods = goodsMapper.selectById(car.getGoodsId());
            if (goods == null || goods.getCards() == null) {
                return 0.5;
            }
            List<String> prefCards = objectMapper.readValue(pref.getPreferredCards(),
                    new TypeReference<List<String>>() {});
            List<String> carCards = objectMapper.readValue(goods.getCards(),
                    new TypeReference<List<String>>() {});

            if (prefCards.isEmpty() || carCards.isEmpty()) {
                return 0.5;
            }

            long matchCount = carCards.stream()
                    .filter(prefCards::contains)
                    .count();

            return (double) matchCount / Math.max(prefCards.size(), carCards.size());
        } catch (Exception e) {
            return 0.5;
        }
    }

    @Override
    public BigDecimal calculateSuccessRate(Car car) {
        double historyRate = calculateHistoryRate(car.getUserId());
        double participationRate = calculateParticipationRate(car);
        double priceCompetitiveness = calculatePriceCompetitiveness(car);
        double creditScore = calculateOwnerCredit(car.getUserId());

        double successRate = historyRate * SUCCESS_WEIGHT_HISTORY
                + participationRate * SUCCESS_WEIGHT_PARTICIPATION
                + priceCompetitiveness * SUCCESS_WEIGHT_PRICE
                + creditScore * SUCCESS_WEIGHT_CREDIT;

        return BigDecimal.valueOf(Math.min(successRate * 100, 99))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateHistoryRate(Long userId) {
        LambdaQueryWrapper<Car> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Car::getUserId, userId).in(Car::getStatus, 1, 2);
        long completedCount = carMapper.selectCount(totalWrapper);

        LambdaQueryWrapper<Car> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(Car::getUserId, userId);
        long totalCount = carMapper.selectCount(allWrapper);

        return totalCount == 0 ? 0.5 : (double) completedCount / totalCount;
    }

    private double calculateParticipationRate(Car car) {
        if (car.getTotalCount() == null || car.getTotalCount() == 0) {
            return 0;
        }
        return (double) car.getCurrentCount() / car.getTotalCount();
    }

    private double calculatePriceCompetitiveness(Car car) {
        if (car.getGoodsId() == null || car.getPricePer() == null) {
            return 0.5;
        }
        Goods goods = goodsMapper.selectById(car.getGoodsId());
        if (goods == null || goods.getMarketPrice() == null || goods.getMarketPrice().compareTo(BigDecimal.ZERO) == 0) {
            return 0.5;
        }
        double ratio = car.getPricePer().doubleValue() / goods.getMarketPrice().doubleValue();
        if (ratio <= 0.5) return 1.0;
        if (ratio <= 0.7) return 0.8;
        if (ratio <= 0.9) return 0.6;
        if (ratio <= 1.0) return 0.4;
        return 0.2;
    }

    private double calculateOwnerCredit(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getCreditScore() == null) {
            return 0.5;
        }
        return user.getCreditScore() / 100.0;
    }

    @Override
    public Map<String, Object> getRecommendedCars(Long userId, Integer page, Integer pageSize) {
        UserPreference pref = userPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserPreference>().eq(UserPreference::getUserId, userId));
        User currentUser = userMapper.selectById(userId);

        // 用户所参与的拼车ID（用于去重，不推荐已参与的）
        List<CarMember> myMembers = carMemberMapper.selectList(
                new LambdaQueryWrapper<CarMember>().eq(CarMember::getUserId, userId));
        Set<Long> joinedCarIds = myMembers.stream().map(CarMember::getCarId).collect(Collectors.toSet());

        LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
        carWrapper.eq(Car::getStatus, 0);
        List<Car> allCars = carMapper.selectList(carWrapper);

        List<Map<String, Object>> scoredCars = new ArrayList<>();
        for (Car car : allCars) {
            if (joinedCarIds.contains(car.getId())) continue; // 已参与的不推荐
            if (car.getUserId().equals(userId)) continue;     // 自己的拼车不推荐

            // AI 智能评分：综合考虑多个维度
            BigDecimal score = calculateSmartScore(userId, currentUser, pref, car);
            Map<String, Object> item = new HashMap<>();
            item.put("car", car);
            item.put("matchScore", score);
            scoredCars.add(item);
        }

        // 按匹配度降序排列
        scoredCars.sort((a, b) -> {
            BigDecimal sa = (BigDecimal) a.get("matchScore");
            BigDecimal sb = (BigDecimal) b.get("matchScore");
            return sb.compareTo(sa);
        });

        int total = scoredCars.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Map<String, Object>> pagedList;
        if (fromIndex >= total) {
            pagedList = Collections.emptyList();
        } else {
            pagedList = scoredCars.subList(fromIndex, toIndex);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * AI 智能评分：综合多维度计算拼车匹配度
     * 维度：偏好匹配 / 车主信用 / 价格竞争力 / 成团热度 / 信用门槛
     */
    private BigDecimal calculateSmartScore(Long userId, User currentUser,
                                            UserPreference pref, Car car) {
        double score = 0;
        int dimensions = 0;

        // 维度1: 偏好匹配度（权重0.4）- 用户设置的偏好与拼车商品匹配
        if (pref != null) {
            BigDecimal prefScore = calculateMatchScore(pref, car);
            score += prefScore.doubleValue() * 0.4;
            dimensions++;
        }

        // 维度2: 车主信用度（权重0.2）- 高信用车主更可靠
        User owner = userMapper.selectById(car.getUserId());
        if (owner != null && owner.getCreditScore() != null) {
            double creditRatio = owner.getCreditScore() / 100.0;
            score += creditRatio * 0.2;
            dimensions++;
        } else {
            score += 0.5 * 0.2; // 默认中等
        }

        // 维度3: 价格竞争力（权重0.2）- 人均价格越低越有吸引力
        if (car.getPricePer() != null && car.getPricePer().compareTo(BigDecimal.ZERO) > 0) {
            // 参考价：如果没有市场价，用人均50作为基准
            BigDecimal avgPrice = BigDecimal.valueOf(50);
            if (car.getGoodsId() != null) {
                Goods goods = goodsMapper.selectById(car.getGoodsId());
                if (goods != null && goods.getMarketPrice() != null && goods.getMarketPrice().compareTo(BigDecimal.ZERO) > 0) {
                    avgPrice = goods.getMarketPrice().divide(BigDecimal.valueOf(car.getTotalCount() != null && car.getTotalCount() > 0 ? car.getTotalCount() : 1), RoundingMode.HALF_UP);
                }
            }
            double priceRatio = avgPrice.doubleValue() > 0
                    ? Math.min(1.0, avgPrice.doubleValue() / car.getPricePer().doubleValue())
                    : 0.5;
            score += Math.min(priceRatio, 1.0) * 0.2;
            dimensions++;
        }

        // 维度4: 成团热度（权重0.1）- 参与度越高越热门
        if (car.getTotalCount() != null && car.getTotalCount() > 0
                && car.getCurrentCount() != null) {
            double progress = (double) car.getCurrentCount() / car.getTotalCount();
            score += progress * 0.1;
            dimensions++;
        }

        // 维度5: 信用门槛兼容（权重0.1）- 用户信用分满足车主设置的门槛
        if (car.getIsRestricted() != null && car.getIsRestricted() == 1
                && car.getMinCreditScore() != null && car.getMinCreditScore() > 0
                && currentUser != null && currentUser.getCreditScore() != null) {
            double compat = currentUser.getCreditScore() >= car.getMinCreditScore() ? 1.0 : 0.0;
            score += compat * 0.1;
        } else {
            score += 0.5 * 0.1; // 无限制
        }
        dimensions++;

        // 标准化的综合评分（0-1范围）
        return BigDecimal.valueOf(Math.min(score, 1.0))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public synchronized Map<String, Integer> smartDistribute(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<CarMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarMember::getCarId, carId);
        List<CarMember> members = carMemberMapper.selectList(wrapper);

        if (members.isEmpty()) {
            return Collections.emptyMap();
        }

        Goods goods = null;
        if (car.getGoodsId() != null) {
            goods = goodsMapper.selectById(car.getGoodsId());
        }

        List<String> versions = new ArrayList<>();
        List<String> cards = new ArrayList<>();
        if (goods != null) {
            try {
                if (goods.getVersions() != null) {
                    versions = objectMapper.readValue(goods.getVersions(), new TypeReference<>() {});
                }
                if (goods.getCards() != null) {
                    cards = objectMapper.readValue(goods.getCards(), new TypeReference<>() {});
                }
            } catch (Exception ignored) {
            }
        }

        Map<Long, String> memberVersionMap = new HashMap<>();
        Map<Long, String> memberCardMap = new HashMap<>();

        if (car.getDistributionType() != null && car.getDistributionType() == 1) {
            distributeByPreference(members, versions, cards, memberVersionMap, memberCardMap);
        } else {
            distributeRandom(members, versions, cards, memberVersionMap, memberCardMap);
        }

        for (CarMember member : members) {
            member.setClaimedVersion(memberVersionMap.getOrDefault(member.getId(), ""));
            member.setClaimedCard(memberCardMap.getOrDefault(member.getId(), ""));
            member.setClaimStatus(1);
            member.setDistributionStatus(1);
            carMemberMapper.updateById(member);
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("distributedCount", members.size());
        return result;
    }

    private void distributeByPreference(List<CarMember> members, List<String> versions,
                                         List<String> cards, Map<Long, String> memberVersionMap,
                                         Map<Long, String> memberCardMap) {
        List<String> availableVersions = new ArrayList<>(versions);
        List<String> availableCards = new ArrayList<>(cards);

        members.sort((a, b) -> {
            int priorityA = a.getPrefPriority() != null ? parsePriority(a.getPrefPriority()) : 0;
            int priorityB = b.getPrefPriority() != null ? parsePriority(b.getPrefPriority()) : 0;
            return priorityB - priorityA;
        });

        for (CarMember member : members) {
            String assignedVersion = assignPreferred(member.getPrefVersions(), availableVersions);
            String assignedCard = assignPreferred(member.getPrefCards(), availableCards);

            memberVersionMap.put(member.getId(), assignedVersion);
            memberCardMap.put(member.getId(), assignedCard);

            if (!assignedVersion.isEmpty()) {
                availableVersions.remove(assignedVersion);
            }
            if (!assignedCard.isEmpty()) {
                availableCards.remove(assignedCard);
            }
        }
    }

    private void distributeRandom(List<CarMember> members, List<String> versions,
                                   List<String> cards, Map<Long, String> memberVersionMap,
                                   Map<Long, String> memberCardMap) {
        List<String> availableVersions = new ArrayList<>(versions);
        List<String> availableCards = new ArrayList<>(cards);
        Random random = new Random();

        for (CarMember member : members) {
            String assignedVersion = "";
            String assignedCard = "";

            if (!availableVersions.isEmpty()) {
                int idx = random.nextInt(availableVersions.size());
                assignedVersion = availableVersions.remove(idx);
            }
            if (!availableCards.isEmpty()) {
                int idx = random.nextInt(availableCards.size());
                assignedCard = availableCards.remove(idx);
            }

            memberVersionMap.put(member.getId(), assignedVersion);
            memberCardMap.put(member.getId(), assignedCard);
        }
    }

    private String assignPreferred(String prefJson, List<String> available) {
        if (prefJson == null || prefJson.isEmpty() || available.isEmpty()) {
            if (!available.isEmpty()) {
                return available.get(0);
            }
            return "";
        }
        try {
            List<String> prefs = objectMapper.readValue(prefJson, new TypeReference<>() {});
            for (String pref : prefs) {
                if (available.contains(pref)) {
                    return pref;
                }
            }
        } catch (Exception ignored) {
        }
        return available.isEmpty() ? "" : available.get(0);
    }

    private int parsePriority(String priority) {
        if (priority == null) return 0;
        try {
            return Integer.parseInt(priority);
        } catch (NumberFormatException e) {
            return priority.length();
        }
    }
}
