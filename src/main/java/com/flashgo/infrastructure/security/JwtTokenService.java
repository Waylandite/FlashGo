package com.flashgo.infrastructure.security;

import com.flashgo.auth.entity.IdentityType;
import com.flashgo.auth.entity.RoleType;
import com.flashgo.common.enums.ErrorCode;
import com.flashgo.common.exception.BizException;
import com.flashgo.config.properties.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private final AuthProperties authProperties;
    private final SecretKey secretKey;

    public JwtTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long subjectId, IdentityType identityType, RoleType roleType) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(authProperties.getTokenExpireSeconds());
        return Jwts.builder()
                .subject(String.valueOf(subjectId))
                .claim("identity_type", identityType.name())
                .claim("role", roleType.getCode())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public JwtClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return JwtClaims.builder()
                    .subjectId(Long.valueOf(claims.getSubject()))
                    .identityType(IdentityType.from(claims.get("identity_type", String.class)))
                    .roleType(RoleType.valueOf(claims.get("role", String.class)))
                    .build();
        } catch (ExpiredJwtException ex) {
            throw new BizException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }

    public long getTokenExpireSeconds() {
        return authProperties.getTokenExpireSeconds();
    }
}
