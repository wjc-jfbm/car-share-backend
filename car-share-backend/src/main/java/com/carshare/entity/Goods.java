package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String versions;
    private String cards;
    private BigDecimal marketPrice;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createdAt;
}