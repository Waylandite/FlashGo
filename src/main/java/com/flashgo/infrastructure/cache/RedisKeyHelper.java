package com.flashgo.infrastructure.cache;

import com.flashgo.auth.entity.IdentityType;
import com.flashgo.config.properties.AuthProperties;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyHelper {

    private final AuthProperties authProperties;

    public RedisKeyHelper(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String loginStatusKey(IdentityType identityType, Long id) {
        return authProperties.getRedisKeyPrefix() + ":" + identityType.name() + ":" + id;
    }

    public String permissionPoolKey() {
        return authProperties.getPermissionPoolKey();
    }
}
