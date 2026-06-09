package com.carshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carshare.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT AVG(rating) FROM review WHERE to_user_id = #{toUserId}")
    Double selectAvgRatingByToUserId(Long toUserId);
}