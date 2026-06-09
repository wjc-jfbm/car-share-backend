package com.carshare.config;

import com.carshare.entity.Car;
import com.carshare.entity.CarMember;
import com.carshare.entity.Goods;
import com.carshare.entity.User;
import com.carshare.mapper.CarMapper;
import com.carshare.mapper.CarMemberMapper;
import com.carshare.mapper.GoodsMapper;
import com.carshare.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据初始化器：首次启动时若数据库为空，自动插入演示数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private GoodsMapper goodsMapper;

    @Autowired(required = false)
    private CarMapper carMapper;

    @Autowired(required = false)
    private CarMemberMapper carMemberMapper;

    @Override
    public void run(String... args) {
        try {
            if (carMapper == null || carMapper.selectCount(null) > 0) {
                return; // 已有数据，跳过
            }

            log.info("数据库为空，正在初始化演示数据...");

            // 1. 创建用户
            User user1 = createUser("wx_dev_user_1", "追星达人", 92, 5);
            User user2 = createUser("wx_dev_user_2", "小确幸", 85, 4);
            User user3 = createUser("wx_dev_user_3", "快乐星球", 78, 4);
            log.info("已创建 {} 个演示用户", 3);

            // 2. 创建商品
            Goods goods1 = createGoods("2024 冬季特别专辑", "专辑",
                    "[\"A版\",\"B版\",\"C版\",\"D版\"]",
                    "[\"成员A小卡\",\"成员B小卡\",\"成员C小卡\",\"成员D小卡\",\"成员E小卡\"]",
                    new BigDecimal("158.00"));
            Goods goods2 = createGoods("夏日写真集豪华版", "写真",
                    "[\"标准版\",\"豪华版\"]",
                    "[\"单人写真卡x5\",\"团体写真卡x3\"]",
                    new BigDecimal("268.00"));
            log.info("已创建 {} 个演示商品", 2);

            // 3. 创建拼车
            Car car1 = createCar(user1.getId(), "【包邮】2024冬季专辑拼车", goods1.getId(), "2024 冬季特别专辑",
                    5, 3, new BigDecimal("790.00"), new BigDecimal("158.00"),
                    LocalDateTime.now().plusDays(7), 1, 0,
                    new BigDecimal("85.50"));

            Car car2 = createCar(user1.getId(), "夏日写真集豪华版拼车", goods2.getId(), "夏日写真集",
                    4, 2, new BigDecimal("1072.00"), new BigDecimal("268.00"),
                    LocalDateTime.now().plusDays(5), 0, 0,
                    new BigDecimal("72.30"));
            log.info("已创建 {} 个演示拼车", 2);

            // 4. 创建成员
            createMember(car1.getId(), user1.getId(), "[\"A版\"]", "[\"成员A小卡\"]", new BigDecimal("158.00"), 1, 1);
            createMember(car1.getId(), user2.getId(), "[\"B版\"]", "[\"成员B小卡\"]", new BigDecimal("158.00"), 1, 0);
            createMember(car1.getId(), user3.getId(), "[\"C版\"]", "[\"成员C小卡\"]", new BigDecimal("158.00"), 0, 0);
            createMember(car2.getId(), user1.getId(), "[\"豪华版\"]", "[\"成员A小卡\"]", new BigDecimal("268.00"), 1, 1);
            createMember(car2.getId(), user2.getId(), "[\"豪华版\"]", "[\"成员B小卡\"]", new BigDecimal("268.00"), 1, 0);
            log.info("已创建 {} 个拼车成员记录", 5);

            log.info("✅ 演示数据初始化完成！现在可以看到拼车列表了。");
        } catch (Exception e) {
            log.warn("数据初始化跳过（可能已有数据或表不存在）: {}", e.getMessage());
        }
    }

    private User createUser(String openid, String nickname, int creditScore, int creditLevel) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname(nickname);
        user.setCreditScore(creditScore);
        user.setCreditLevel(creditLevel);
        user.setStatus(1);
        user.setTotalTransactions(0);
        user.setSuccessTransactions(0);
        userMapper.insert(user);
        return user;
    }

    private Goods createGoods(String name, String type, String versions, String cards, BigDecimal price) {
        Goods goods = new Goods();
        goods.setName(name);
        goods.setType(type);
        goods.setVersions(versions);
        goods.setCards(cards);
        goods.setMarketPrice(price);
        goods.setStatus(1);
        goodsMapper.insert(goods);
        return goods;
    }

    private Car createCar(Long userId, String title, Long goodsId, String goodsName,
                          int totalCount, int currentCount, BigDecimal priceTotal, BigDecimal pricePer,
                          LocalDateTime deadline, int distributionType, int status, BigDecimal successRate) {
        Car car = new Car();
        car.setUserId(userId);
        car.setTitle(title);
        car.setGoodsId(goodsId);
        car.setGoodsName(goodsName);
        car.setTotalCount(totalCount);
        car.setCurrentCount(currentCount);
        car.setPriceTotal(priceTotal);
        car.setPricePer(pricePer);
        car.setDeadline(deadline);
        car.setDistributionType(distributionType);
        car.setStatus(status);
        car.setSuccessRate(successRate);
        carMapper.insert(car);
        return car;
    }

    private void createMember(Long carId, Long userId, String prefVersions, String prefCards,
                              BigDecimal amount, int payStatus, int isOwner) {
        CarMember member = new CarMember();
        member.setCarId(carId);
        member.setUserId(userId);
        member.setPrefVersions(prefVersions);
        member.setPrefCards(prefCards);
        member.setAmount(amount);
        member.setPayStatus(payStatus);
        member.setIsOwner(isOwner);
        member.setClaimStatus(isOwner == 1 ? 2 : 1);
        member.setJoinTime(LocalDateTime.now());
        member.setCreatedAt(LocalDateTime.now());
        carMemberMapper.insert(member);
    }
}
