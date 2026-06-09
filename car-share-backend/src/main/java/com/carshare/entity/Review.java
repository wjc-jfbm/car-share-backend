package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long fromUserId;
    private Long toUserId;
    private Integer type;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}