package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("car_favorite")
public class CarFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long userId;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String carTitle;
}
