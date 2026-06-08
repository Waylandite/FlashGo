package com.flashgo.auth.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashgo.auth.entity.IdentityType;
import com.flashgo.auth.service.LoginStatusService;
import com.flashgo.common.enums.ErrorCode;
import com.flashgo.common.exception.BizException;
import com.flashgo.config.properties.AuthProperties;
import com.flashgo.infrastructure.cache.RedisKeyHelper;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisLoginStatusService implements LoginStatusService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisKeyHelper redisKeyHelper;
    private final Duration ttl;

    public RedisLoginStatusService(StringRedisTemplate stringRedisTemplate,
                                   ObjectMapper objectMapper,
                                   RedisKeyHelper redisKeyHelper,
                                   AuthProperties authProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.redisKeyHelper = redisKeyHelper;
        this.ttl = Duration.ofSeconds(authProperties.getTokenExpireSeconds());
    }

    @Override
    public void activate(Long id, IdentityType identityType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "1");
        payload.put("bannedPermissions", Collections.emptyList());
        write(id, identityType, payload);
    }

    @Override
    public void logout(Long id, IdentityType identityType) {
        stringRedisTemplate.delete(redisKeyHelper.loginStatusKey(identityType, id));
    }

    @Override
    public Set<String> getBannedPermissions(Long id, IdentityType identityType) {
        Map<String, Object> payload = readRequired(id, identityType);
        Object raw = payload.get("bannedPermissions");
        if (raw == null) {
            return Collections.emptySet();
        }
        return objectMapper.convertValue(raw, new TypeReference<Set<String>>() {
        });
    }

    @Override
    public void refresh(Long id, IdentityType identityType) {
        String key = redisKeyHelper.loginStatusKey(identityType, id);
        Boolean exists = stringRedisTemplate.hasKey(key);
        if (!Boolean.TRUE.equals(exists)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        stringRedisTemplate.expire(key, ttl);
    }

    private void write(Long id, IdentityType identityType, Map<String, Object> payload) {
        try {
            stringRedisTemplate.opsForValue()
                    .set(redisKeyHelper.loginStatusKey(identityType, id), objectMapper.writeValueAsString(payload), ttl);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "写入登录状态失败");
        }
    }

    private Map<String, Object> readRequired(Long id, IdentityType identityType) {
        String key = redisKeyHelper.loginStatusKey(identityType, id);
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }
}
