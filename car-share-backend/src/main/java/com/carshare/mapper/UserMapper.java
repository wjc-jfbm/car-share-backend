package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}