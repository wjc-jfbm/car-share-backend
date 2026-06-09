package com.carshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String unionid;
    private String sessionKey;
    private String nickname;
    private String avatar;
    private String phone;
    private String password;
    private String realName;
    private Integer creditScore;
    private Integer creditLevel;
    private Integer role;
    private Integer status;
    private Integer totalTransactions;
    private Integer successTransactions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
