package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_preference")
public class UserPreference {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String preferredVersions;
    private String preferredCards;
    private String preferredArtists;
    private Integer autoMatch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}