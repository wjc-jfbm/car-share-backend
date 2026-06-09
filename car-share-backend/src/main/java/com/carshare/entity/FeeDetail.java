package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_detail")
public class FeeDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long carMemberId;
    private Long userId;
    private BigDecimal goodsAmount;    // 商品金额
    private BigDecimal shippingFee;    // 运费分摊
    private BigDecimal totalAmount;    // 应付总额
    private Integer shippingFeeType;   // 0-均摊 1-按份数比例 2-车主承担
    private BigDecimal depositAmount;  // 定金
    private BigDecimal balanceAmount;  // 尾款
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
