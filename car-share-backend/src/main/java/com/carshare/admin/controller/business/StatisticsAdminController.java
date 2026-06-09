package com.carshare.admin.controller.business;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.carshare.common.core.controller.BaseController;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.entity.Car;
import com.carshare.entity.CarOrder;
import com.carshare.entity.Goods;
import com.carshare.entity.Logistics;
import com.carshare.entity.User;
import com.carshare.mapper.CarMapper;
import com.carshare.mapper.CarOrderMapper;
import com.carshare.mapper.GoodsMapper;
import com.carshare.mapper.LogisticsMapper;
import com.carshare.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据统计Controller
 */
@RestController
@RequestMapping("/business/statistics")
public class StatisticsAdminController extends BaseController {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarOrderMapper carOrderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/dashboard")
    public AjaxResult getDashboard() {
        Map<String, Object> data = new HashMap<>();

        // 拼车统计
        long carTotal = carMapper.selectCount(new QueryWrapper<>());
        long carActive = carMapper.selectCount(new QueryWrapper<Car>().eq("status", 1));
        long carCompleted = carMapper.selectCount(new QueryWrapper<Car>().eq("status", 3));
        data.put("carTotal", carTotal);
        data.put("carActive", carActive);
        data.put("carCompleted", carCompleted);

        // 订单统计
        long orderTotal = carOrderMapper.selectCount(new QueryWrapper<>());
        long orderPaid = carOrderMapper.selectCount(new QueryWrapper<CarOrder>().eq("status", 1));
        long orderSettled = carOrderMapper.selectCount(new QueryWrapper<CarOrder>().eq("settle_status", 1));
        data.put("orderTotal", orderTotal);
        data.put("orderPaid", orderPaid);
        data.put("orderSettled", orderSettled);

        // 商品统计
        long goodsTotal = goodsMapper.selectCount(new QueryWrapper<>());
        long goodsOnSale = goodsMapper.selectCount(new QueryWrapper<Goods>().eq("status", 1));
        data.put("goodsTotal", goodsTotal);
        data.put("goodsOnSale", goodsOnSale);

        // 物流统计
        long logisticsTotal = logisticsMapper.selectCount(new QueryWrapper<>());
        long logisticsShipped = logisticsMapper.selectCount(new QueryWrapper<Logistics>().eq("status", 1));
        long logisticsDelivered = logisticsMapper.selectCount(new QueryWrapper<Logistics>().eq("status", 2));
        data.put("logisticsTotal", logisticsTotal);
        data.put("logisticsShipped", logisticsShipped);
        data.put("logisticsDelivered", logisticsDelivered);

        // 今日新增统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayNewCar = carMapper.selectCount(new QueryWrapper<Car>().ge("created_at", todayStart));
        long todayNewOrder = carOrderMapper.selectCount(new QueryWrapper<CarOrder>().ge("created_at", todayStart));
        data.put("todayNewCar", todayNewCar);
        data.put("todayNewOrder", todayNewOrder);

        // 小程序用户统计
        long userTotal = userMapper.selectCount(new QueryWrapper<>());
        long userActive = userMapper.selectCount(new QueryWrapper<User>().eq("status", 1));
        long userLeader = userMapper.selectCount(new QueryWrapper<User>().eq("role", 1));
        long todayNewUser = userMapper.selectCount(new QueryWrapper<User>().ge("created_at", todayStart));
        data.put("userTotal", userTotal);
        data.put("userActive", userActive);
        data.put("userLeader", userLeader);
        data.put("todayNewUser", todayNewUser);

        return success(data);
    }

    /**
     * 获取近7天订单趋势
     */
    @GetMapping("/orderTrend")
    public AjaxResult getOrderTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Long> carCounts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            long orderCount = carOrderMapper.selectCount(
                    new QueryWrapper<CarOrder>().between("created_at", dayStart, dayEnd));
            long carCount = carMapper.selectCount(
                    new QueryWrapper<Car>().between("created_at", dayStart, dayEnd));

            orderCounts.add(orderCount);
            carCounts.add(carCount);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("orderCounts", orderCounts);
        data.put("carCounts", carCounts);

        return success(data);
    }

    /**
     * 获取拼车状态分布
     */
    @GetMapping("/carStatusDist")
    public AjaxResult getCarStatusDist() {
        List<Map<String, Object>> result = new ArrayList<>();

        String[] statusNames = {"招募中", "进行中", "已截止", "已完成"};
        for (int i = 0; i < statusNames.length; i++) {
            long count = carMapper.selectCount(new QueryWrapper<Car>().eq("status", i + 1));
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", count);
            result.add(item);
        }

        return success(result);
    }

    /**
     * 获取订单状态分布
     */
    @GetMapping("/orderStatusDist")
    public AjaxResult getOrderStatusDist() {
        List<Map<String, Object>> result = new ArrayList<>();

        String[] statusNames = {"待支付", "已支付", "已取消"};
        for (int i = 0; i < statusNames.length; i++) {
            long count = carOrderMapper.selectCount(new QueryWrapper<CarOrder>().eq("status", i));
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", count);
            result.add(item);
        }

        return success(result);
    }

    /**
     * 获取最近拼车活动
     */
    @GetMapping("/recentCars")
    public AjaxResult getRecentCars() {
        List<Car> cars = carMapper.selectList(
                new QueryWrapper<Car>().orderByDesc("created_at").last("LIMIT 5"));
        return success(cars);
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/recentOrders")
    public AjaxResult getRecentOrders() {
        List<CarOrder> orders = carOrderMapper.selectList(
                new QueryWrapper<CarOrder>().orderByDesc("created_at").last("LIMIT 5"));
        return success(orders);
    }
}
