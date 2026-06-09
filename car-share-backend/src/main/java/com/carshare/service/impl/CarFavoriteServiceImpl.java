package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Car;
import com.carshare.entity.CarFavorite;
import com.carshare.mapper.CarFavoriteMapper;
import com.carshare.mapper.CarMapper;
import com.carshare.service.CarFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CarFavoriteServiceImpl implements CarFavoriteService {

    @Autowired
    private CarFavoriteMapper carFavoriteMapper;
    @Autowired
    private CarMapper carMapper;

    @Override
    public boolean addFavorite(Long carId, Long userId) {
        if (isFavorite(carId, userId)) return true;
        CarFavorite fav = new CarFavorite();
        fav.setCarId(carId);
        fav.setUserId(userId);
        fav.setCreatedAt(LocalDateTime.now());
        return carFavoriteMapper.insert(fav) > 0;
    }

    @Override
    public boolean removeFavorite(Long carId, Long userId) {
        LambdaQueryWrapper<CarFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarFavorite::getCarId, carId).eq(CarFavorite::getUserId, userId);
        return carFavoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isFavorite(Long carId, Long userId) {
        LambdaQueryWrapper<CarFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarFavorite::getCarId, carId).eq(CarFavorite::getUserId, userId);
        return carFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Map<String, Object> getMyFavorites(Long userId, Integer page, Integer pageSize) {
        Page<CarFavorite> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CarFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarFavorite::getUserId, userId).orderByDesc(CarFavorite::getCreatedAt);
        Page<CarFavorite> result = carFavoriteMapper.selectPage(pageObj, wrapper);

        for (CarFavorite fav : result.getRecords()) {
            Car car = carMapper.selectById(fav.getCarId());
            if (car != null) {
                fav.setCarTitle(car.getTitle());
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }
}
