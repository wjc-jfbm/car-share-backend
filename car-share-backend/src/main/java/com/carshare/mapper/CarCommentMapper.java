package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.CarComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarCommentMapper extends BaseMapper<CarComment> {
}
