package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("car_comment")
public class CarComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long userId;
    private Integer type;          // 0-评论 1-动态 2-晒单
    private String content;
    private String imageUrl;
    private Long replyToId;        // 回复的评论ID
    private LocalDateTime createdAt;
}
