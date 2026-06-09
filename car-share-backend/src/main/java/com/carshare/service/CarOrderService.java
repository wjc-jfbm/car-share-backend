package com.carshare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.CarOrder;
import java.util.List;

public interface CarOrderService {
    CarOrder getOrderDetail(Long orderId);
    List<CarOrder> adminList(CarOrder order);
    Page<CarOrder> adminListPage(Page<CarOrder> page, CarOrder order);
    boolean updateOrder(CarOrder order);
    boolean deleteOrders(Long[] orderIds);
}
