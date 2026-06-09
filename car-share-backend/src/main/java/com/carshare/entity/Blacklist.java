package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blacklist")
public class Blacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long blockedUserId;
    private String reason;
    private LocalDateTime createdAt;
}
