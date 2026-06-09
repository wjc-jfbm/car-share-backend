package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.CarOrder;
import com.carshare.mapper.CarOrderMapper;
import com.carshare.service.CarOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CarOrderServiceImpl implements CarOrderService {

    @Autowired
    private CarOrderMapper carOrderMapper;

    @Override
    public CarOrder getOrderDetail(Long orderId) {
        return carOrderMapper.selectById(orderId);
    }

    @Override
    public List<CarOrder> adminList(CarOrder order) {
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        if (order.getStatus() != null) {
            wrapper.eq(CarOrder::getStatus, order.getStatus());
        }
        if (order.getOrderNo() != null && !order.getOrderNo().isEmpty()) {
            wrapper.like(CarOrder::getOrderNo, order.getOrderNo());
        }
        wrapper.orderByDesc(CarOrder::getCreatedAt);
        return carOrderMapper.selectList(wrapper);
    }

    @Override
    public Page<CarOrder> adminListPage(Page<CarOrder> page, CarOrder order) {
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        if (order.getStatus() != null) {
            wrapper.eq(CarOrder::getStatus, order.getStatus());
        }
        if (order.getOrderNo() != null && !order.getOrderNo().isEmpty()) {
            wrapper.like(CarOrder::getOrderNo, order.getOrderNo());
        }
        wrapper.orderByDesc(CarOrder::getCreatedAt);
        return carOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean updateOrder(CarOrder order) {
        return carOrderMapper.updateById(order) > 0;
    }

    @Override
    public boolean deleteOrders(Long[] orderIds) {
        return carOrderMapper.deleteBatchIds(Arrays.asList(orderIds)) > 0;
    }
}
