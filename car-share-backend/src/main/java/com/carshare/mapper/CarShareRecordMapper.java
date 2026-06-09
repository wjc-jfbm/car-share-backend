package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.CarShareRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarShareRecordMapper extends BaseMapper<CarShareRecord> {
}
