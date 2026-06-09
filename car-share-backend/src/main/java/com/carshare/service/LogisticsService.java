package com.carshare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Logistics;

import java.util.Map;

public interface LogisticsService {
    Logistics getByCarId(Long carId);
    Long createLogistics(Long userId, Logistics logistics);
    boolean updateLogistics(Long id, Long userId, Logistics logistics);
    boolean updateStatus(Long id, Long userId, Integer status);
    Map<String, Object> getLogisticsList(Integer page, Integer pageSize);
    Map<String, Object> getLogisticsByCarId(Long carId);

    Page<Logistics> adminListPage(Page<Logistics> page, Integer status, String expressNo);
    Map<String, Object> getAdminDetail(Long id);
    boolean adminCreateLogistics(Logistics logistics);
    boolean adminUpdateLogistics(Logistics logistics);
    boolean adminUpdateStatus(Long id, Integer status);
    boolean adminDeleteLogistics(Long[] ids);
}
