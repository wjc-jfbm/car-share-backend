package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund")
public class Refund {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long carMemberId;
    private Long userId;
    private BigDecimal amount;
    private Integer type;          // 1-主动退出 2-拼车失败自动退款
    private String reason;
    private Integer status;        // 0-申请中 1-审核通过 2-退款中 3-已到账 4-已驳回
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
