package com.carshare.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Car;
import com.carshare.entity.CarMember;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CarService {
    Map<String, Object> getCarList(Integer page, Integer pageSize, Integer status, String keyword,
                                    BigDecimal priceMin, BigDecimal priceMax, String sortBy);
    Car getCarDetail(Long carId);
    Long createCar(Long userId, Car car);
    boolean joinCar(Long carId, Long userId, CarMember member);
    boolean closeCar(Long carId, Long userId);
    boolean claimItem(Long carId, Long userId, String claimedVersion, String claimedCard);
    boolean uploadPayEvidence(Long carMemberId, Long userId, String evidenceUrl);
    boolean reviewPayEvidence(Long carMemberId, Long userId, Integer status, String rejectReason);
    Map<String, Object> distribute(Long carId, Long userId);
    Map<String, Object> getMyCars(Long userId, Integer page, Integer pageSize);
    Map<String, Object> getMyAllCars(Long userId, Integer page, Integer pageSize);
    Map<String, Object> getMyJoinedCars(Long userId, Integer page, Integer pageSize);
    Map<String, Object> getHistoryCars(Long userId, Integer page, Integer pageSize);
    List<Map<String, Object>> exportMyCars(Long userId);
    boolean completeCar(Long carId, Long userId);
    boolean cancelCar(Long carId, Long userId);
    List<Car> adminList(Car car);
    IPage<Car> adminListPage(Page<Car> page, Car car);
    boolean updateCar(Car car, Long userId);
    boolean deleteCars(Long[] carIds);
}
