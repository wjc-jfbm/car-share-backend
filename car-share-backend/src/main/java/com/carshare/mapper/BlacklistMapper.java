package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {
}
