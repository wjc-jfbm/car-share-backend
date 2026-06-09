package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.CarOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarOrderMapper extends BaseMapper<CarOrder> {
}