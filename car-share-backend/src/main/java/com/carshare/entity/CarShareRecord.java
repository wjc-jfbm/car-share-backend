package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("car_share_record")
public class CarShareRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long carId;
    private Long userId;
    private String shareCode;      // 分享码/口令
    private String shareType;      // friend/poster/code
    private Long inviteUserId;     // 通过分享加入的用户ID
    private LocalDateTime createdAt;
}
