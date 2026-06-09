package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("car")
public class Car {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private Long goodsId;
    private String goodsName;
    private String goodsImage;
    private String description;
    private Integer totalCount;
    private Integer currentCount;
    private BigDecimal priceTotal;
    private BigDecimal pricePer;
    private BigDecimal depositAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private Integer distributionType;
    private Integer isRestricted;
    private Integer minCreditScore;
    private Integer status;
    private BigDecimal successRate;
    private BigDecimal matchScore;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private LocalDateTime completedAt;

    @TableField(exist = false)
    private String userNickname;

    @TableField(exist = false)
    private Integer userCredit;

    @TableField(exist = false)
    private List<CarMember> members;

    @TableField(exist = false)
    private Goods goods;
}
