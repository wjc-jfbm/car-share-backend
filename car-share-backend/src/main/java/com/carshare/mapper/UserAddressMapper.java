package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
