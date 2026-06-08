package com.flashgo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "flashgo.auth")
public class AuthProperties {

    private String jwtSecret;
    private long tokenExpireSeconds;
    private String redisKeyPrefix;
    private String permissionPoolKey;
}
