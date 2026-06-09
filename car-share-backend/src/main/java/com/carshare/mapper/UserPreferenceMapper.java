package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
}