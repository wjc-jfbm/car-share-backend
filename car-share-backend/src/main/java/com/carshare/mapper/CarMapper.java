package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Car;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface CarMapper extends BaseMapper<Car> {
    IPage<Car> selectCarPage(Page<?> page, @Param("status") Integer status,
                            @Param("keyword") String keyword);

    IPage<Car> selectAdminCarPage(Page<?> page, @Param("car") Car car);

    void autoCloseExpiredCars(@Param("now") LocalDateTime now);
}