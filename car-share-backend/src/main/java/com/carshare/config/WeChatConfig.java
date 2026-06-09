package com.carshare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.mini")
public class WeChatConfig {

    private String appid;
    private String secret;
    private String authUrl;
    private String accessTokenUrl;
}
