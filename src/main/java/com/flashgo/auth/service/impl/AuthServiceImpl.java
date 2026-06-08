package com.flashgo.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashgo.auth.entity.AppUserAccount;
import com.flashgo.auth.entity.IdentityType;
import com.flashgo.auth.entity.MerchantAccount;
import com.flashgo.auth.entity.PlatformAccount;
import com.flashgo.auth.entity.RiderAccount;
import com.flashgo.auth.entity.RoleType;
import com.flashgo.auth.mapper.AppUserAccountMapper;
import com.flashgo.auth.mapper.MerchantAccountMapper;
import com.flashgo.auth.mapper.PlatformAccountMapper;
import com.flashgo.auth.mapper.RiderAccountMapper;
import com.flashgo.auth.model.dto.AuthLoginRequest;
import com.flashgo.auth.model.vo.AuthLoginResponse;
import com.flashgo.auth.model.vo.AuthMeResponse;
import com.flashgo.auth.service.AuthService;
import com.flashgo.auth.service.LoginStatusService;
import com.flashgo.common.context.LoginContext;
import com.flashgo.common.enums.ErrorCode;
import com.flashgo.common.exception.BizException;
import com.flashgo.infrastructure.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserAccountMapper appUserAccountMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final RiderAccountMapper riderAccountMapper;
    private final PlatformAccountMapper platformAccountMapper;
    private final JwtTokenService jwtTokenService;
    private final LoginStatusService loginStatusService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthLoginResponse login(AuthLoginRequest request) {
        IdentityType identityType = IdentityType.from(request.getType());
        AuthMeResponse userInfo = switch (identityType) {
            case APP_USER -> loginAppUser(request);
            case MERCHANT -> loginMerchant(request);
            case RIDER -> loginRider(request);
            case PLATFORM -> loginPlatform(request);
        };
        RoleType roleType = RoleType.fromIdentityType(identityType);
        String accessToken = jwtTokenService.generateToken(userInfo.getId(), identityType, roleType);
        loginStatusService.activate(userInfo.getId(), identityType);
        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getTokenExpireSeconds())
                .identityType(identityType.name())
                .role(roleType.getCode())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public AuthMeResponse me() {
        var principal = LoginContext.current();
        return switch (principal.getIdentityType()) {
            case APP_USER -> getAppUserMe(principal.getId());
            case MERCHANT -> getMerchantMe(principal.getId());
            case RIDER -> getRiderMe(principal.getId());
            case PLATFORM -> getPlatformMe(principal.getId());
        };
    }

    @Override
    public void logout() {
        var principal = LoginContext.current();
        loginStatusService.logout(principal.getId(), principal.getIdentityType());
    }

    private AuthMeResponse loginAppUser(AuthLoginRequest request) {
        AppUserAccount account = appUserAccountMapper.selectOne(new LambdaQueryWrapper<AppUserAccount>()
                .eq(AppUserAccount::getMobile, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.APP_USER.name())
                .role(RoleType.ROLE_USER.getCode())
                .username(account.getMobile())
                .displayName(account.getNickname())
                .build();
    }

    private AuthMeResponse loginMerchant(AuthLoginRequest request) {
        MerchantAccount account = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getContactMobile, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.MERCHANT.name())
                .role(RoleType.ROLE_MERCHANT.getCode())
                .username(account.getContactMobile())
                .displayName(account.getName())
                .build();
    }

    private AuthMeResponse loginRider(AuthLoginRequest request) {
        RiderAccount account = riderAccountMapper.selectOne(new LambdaQueryWrapper<RiderAccount>()
                .eq(RiderAccount::getMobile, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.RIDER.name())
                .role(RoleType.ROLE_RIDER.getCode())
                .username(account.getMobile())
                .displayName(account.getName())
                .build();
    }

    private AuthMeResponse loginPlatform(AuthLoginRequest request) {
        PlatformAccount account = platformAccountMapper.selectOne(new LambdaQueryWrapper<PlatformAccount>()
                .eq(PlatformAccount::getUsername, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.PLATFORM.name())
                .role(RoleType.ROLE_PLATFORM.getCode())
                .username(account.getUsername())
                .displayName(account.getDisplayName())
                .build();
    }

    private void validateAccount(String status, String passwordHash, String rawPassword) {
        if (passwordHash == null) {
            throw new BizException(ErrorCode.AUTH_PASSWORD_INVALID);
        }
        if (!"ENABLED".equalsIgnoreCase(status)) {
            throw new BizException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new BizException(ErrorCode.AUTH_PASSWORD_INVALID);
        }
    }

    private AuthMeResponse getAppUserMe(Long id) {
        AppUserAccount account = appUserAccountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.APP_USER.name())
                .role(RoleType.ROLE_USER.getCode())
                .username(account.getMobile())
                .displayName(account.getNickname())
                .build();
    }

    private AuthMeResponse getMerchantMe(Long id) {
        MerchantAccount account = merchantAccountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.MERCHANT.name())
                .role(RoleType.ROLE_MERCHANT.getCode())
                .username(account.getContactMobile())
                .displayName(account.getName())
                .build();
    }

    private AuthMeResponse getRiderMe(Long id) {
        RiderAccount account = riderAccountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.RIDER.name())
                .role(RoleType.ROLE_RIDER.getCode())
                .username(account.getMobile())
                .displayName(account.getName())
                .build();
    }

    private AuthMeResponse getPlatformMe(Long id) {
        PlatformAccount account = platformAccountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.PLATFORM.name())
                .role(RoleType.ROLE_PLATFORM.getCode())
                .username(account.getUsername())
                .displayName(account.getDisplayName())
                .build();
    }
}
