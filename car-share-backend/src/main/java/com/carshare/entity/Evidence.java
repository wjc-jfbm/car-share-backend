package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("evidence")
public class Evidence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carMemberId;
    private Long carId;
    private Long userId;
    private Integer type;
    private String imageUrl;
    private String remark;
    private Integer status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}