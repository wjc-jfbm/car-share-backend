package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class CarOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long carMemberId;
    private Long userId;
    private String orderNo;
    private BigDecimal amount;
    private Integer status;
    private Integer settleStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}