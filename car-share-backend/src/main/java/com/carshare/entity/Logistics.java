package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("logistics")
public class Logistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private String expressNo;
    private String expressCompany;
    private String expressCompanyCode;
    private String remark;
    private Integer status;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String nodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String carTitle;

    @TableField(exist = false)
    private String ownerName;
}