package com.flashgo.auth.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashgo.auth.entity.RoleType;
import com.flashgo.auth.service.PermissionPoolService;
import com.flashgo.infrastructure.cache.RedisKeyHelper;
import java.util.Collections;
import java.util.Set;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPermissionPoolService implements PermissionPoolService {

    private static final TypeReference<Set<String>> STRING_SET_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyHelper redisKeyHelper;
    private final ObjectMapper objectMapper;

    public RedisPermissionPoolService(StringRedisTemplate stringRedisTemplate,
                                      RedisKeyHelper redisKeyHelper,
                                      ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisKeyHelper = redisKeyHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Set<String> getPermissions(RoleType roleType) {
        HashOperations<String, Object, Object> operations = stringRedisTemplate.opsForHash();
        Object value = operations.get(redisKeyHelper.permissionPoolKey(), roleType.getCode());
        if (value == null) {
            return Collections.emptySet();
        }
        try {
            return objectMapper.readValue(value.toString(), STRING_SET_TYPE);
        } catch (Exception ex) {
            return Collections.emptySet();
        }
    }
}
