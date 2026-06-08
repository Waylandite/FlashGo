package com.flashgo.auth.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashgo.auth.entity.RoleType;
import com.flashgo.infrastructure.cache.RedisKeyHelper;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionInitializer implements ApplicationRunner {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyHelper redisKeyHelper;
    private final ObjectMapper objectMapper;

    public RolePermissionInitializer(StringRedisTemplate stringRedisTemplate,
                                     RedisKeyHelper redisKeyHelper,
                                     ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisKeyHelper = redisKeyHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        Map<String, List<String>> permissions = Map.of(
                RoleType.ROLE_USER.getCode(), List.of("order:create", "order:view", "order:cancel", "profile:view"),
                RoleType.ROLE_MERCHANT.getCode(), List.of("order:view", "order:accept", "order:reject"),
                RoleType.ROLE_RIDER.getCode(), List.of("order:view", "order:accept", "order:delivery"),
                RoleType.ROLE_PLATFORM.getCode(), List.of("order:view:all", "order:settle", "account:manage")
        );
        String key = redisKeyHelper.permissionPoolKey();
        for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
            stringRedisTemplate.opsForHash().put(key, entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
        }
    }
}
