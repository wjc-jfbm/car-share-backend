package com.carshare.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.common.enums.CarStatus;
import com.carshare.entity.Car;
import com.carshare.entity.CarMember;
import com.carshare.mapper.CarMapper;
import com.carshare.mapper.CarMemberMapper;
import com.carshare.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CarScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(CarScheduledTask.class);

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarMemberMapper carMemberMapper;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void autoCloseExpiredCars() {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getStatus, CarStatus.RECRUITING.getCode())
               .lt(Car::getDeadline, LocalDateTime.now())
               .isNotNull(Car::getDeadline);

        List<Car> expiredCars = carMapper.selectList(wrapper);

        for (Car car : expiredCars) {
            car.setStatus(CarStatus.CLOSED.getCode());
            carMapper.updateById(car);

            notificationService.sendToCarMembers(
                    car.getId(), null, "拼车已自动截止",
                    "拼车「" + car.getTitle() + "」已到截止时间，系统已自动截止报名", 2);

            logger.info("拼车[{}]已自动截止，标题：{}", car.getId(), car.getTitle());
        }

        if (!expiredCars.isEmpty()) {
            logger.info("自动截止拼车数量：{}", expiredCars.size());
        }
    }

    @Scheduled(cron = "0 0 */6 * * ?")
    @Transactional
    public void remindUnpaidMembers() {
        LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
        carWrapper.in(Car::getStatus, CarStatus.CLOSED.getCode(), CarStatus.SETTLED.getCode());

        List<Car> activeCars = carMapper.selectList(carWrapper);

        for (Car car : activeCars) {
            LambdaQueryWrapper<CarMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(CarMember::getCarId, car.getId())
                         .eq(CarMember::getPayStatus, 0)
                         .eq(CarMember::getIsOwner, 0);
            List<CarMember> unpaidMembers = carMemberMapper.selectList(memberWrapper);

            for (CarMember member : unpaidMembers) {
                if (member.getJoinTime() != null
                        && member.getJoinTime().plusHours(24).isBefore(LocalDateTime.now())) {
                    notificationService.sendNotification(
                            member.getUserId(), car.getId(), "付款提醒",
                            "您参与的拼车「" + car.getTitle() + "」尚未付款，人均 ¥"
                                    + car.getPricePer() + "，请尽快完成付款", 6);
                }
            }
        }

        logger.debug("付款提醒检查完成");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoCancelStaleCars() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getStatus, CarStatus.RECRUITING.getCode())
               .lt(Car::getCreatedAt, sevenDaysAgo);

        List<Car> staleCars = carMapper.selectList(wrapper);

        for (Car car : staleCars) {
            if (car.getCurrentCount() <= 1) {
                car.setStatus(CarStatus.CANCELLED.getCode());
                carMapper.updateById(car);

                notificationService.sendNotification(
                        car.getUserId(), car.getId(), "拼车已自动取消",
                        "您发起的拼车「" + car.getTitle() + "」超过7天无人参与，系统已自动取消", 2);

                logger.info("过期拼车[{}]已自动取消，标题：{}", car.getId(), car.getTitle());
            }
        }

        if (!staleCars.isEmpty()) {
            logger.info("自动取消过期拼车数量：{}", staleCars.size());
        }
    }
}
