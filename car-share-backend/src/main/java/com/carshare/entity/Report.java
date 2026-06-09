package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long targetUserId;
    private Long carId;
    private Integer type;          // 1-虚假凭证 2-恶意行为 3-违规内容 4-其他
    private String description;
    private String imageUrl;
    private Integer status;        // 0-待处理 1-已处理 2-已驳回
    private String handleResult;
    private Long handledBy;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
