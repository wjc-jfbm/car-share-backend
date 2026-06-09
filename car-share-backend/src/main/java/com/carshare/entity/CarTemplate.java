package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("car_template")
public class CarTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String goodsName;
    private String goodsImage;
    private String description;
    private Integer totalCount;
    private BigDecimal priceTotal;
    private BigDecimal pricePer;
    private Integer distributionType;
    private Integer isRestricted;
    private Integer minCreditScore;
    private String versions;
    private String cards;
    private Integer isTop;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
