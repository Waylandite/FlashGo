package com.flashgo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@ConfigurationProperties(prefix = "flashgo.auth")
@Validated
public class AuthProperties {

    @NotBlank
    @Size(min = 32)
    private String jwtSecret;
    @Min(1)
    private long tokenExpireSeconds;
    @NotBlank
    private String redisKeyPrefix;
    @NotBlank
    private String permissionPoolKey;
}
