package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("car_member")
public class CarMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long userId;
    private String prefVersions;
    private String prefCards;
    private String prefPriority;
    private String phone;
    private String address;
    private Integer claimStatus;
    private String claimedVersion;
    private String claimedCard;
    private BigDecimal matchScore;
    private BigDecimal amount;
    private BigDecimal depositPaid;
    private BigDecimal balancePaid;
    private String evidenceUrl;
    private Integer evidenceStatus;
    private String evidenceRejectReason;
    private Integer payStatus;
    private Integer distributionStatus;
    private Integer isOwner;
    private LocalDateTime joinTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String nickname;
}
