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
import com.flashgo.auth.model.dto.AuthRegisterRequest;
import com.flashgo.auth.model.vo.AuthLoginResponse;
import com.flashgo.auth.model.vo.AuthMeResponse;
import com.flashgo.auth.model.vo.AuthRegisterResponse;
import com.flashgo.auth.service.AuthService;
import com.flashgo.auth.service.LoginStatusService;
import com.flashgo.auth.support.AuthNoGenerator;
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
    private final AuthNoGenerator authNoGenerator;

    @Override
    public AuthRegisterResponse register(AuthRegisterRequest request) {
        IdentityType identityType = IdentityType.from(request.getType());
        return switch (identityType) {
            case APP_USER -> registerAppUser(request);
            case MERCHANT -> registerMerchant(request);
            case RIDER -> registerRider(request);
            case PLATFORM -> registerPlatform(request);
        };
    }

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
        String accessToken = jwtTokenService.generateToken(userInfo.getId(), identityType);
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
                .eq(AppUserAccount::getUsername, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.APP_USER.name())
                .role(RoleType.ROLE_USER.getCode())
                .username(account.getUsername())
                .displayName(account.getNickname())
                .build();
    }

    private AuthMeResponse loginMerchant(AuthLoginRequest request) {
        MerchantAccount account = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getUsername, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.MERCHANT.name())
                .role(RoleType.ROLE_MERCHANT.getCode())
                .username(account.getUsername())
                .displayName(account.getName())
                .build();
    }

    private AuthMeResponse loginRider(AuthLoginRequest request) {
        RiderAccount account = riderAccountMapper.selectOne(new LambdaQueryWrapper<RiderAccount>()
                .eq(RiderAccount::getUsername, request.getUsername())
                .last("limit 1"));
        validateAccount(account == null ? null : account.getStatus(), account == null ? null : account.getPasswordHash(), request.getPassword());
        return AuthMeResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.RIDER.name())
                .role(RoleType.ROLE_RIDER.getCode())
                .username(account.getUsername())
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
                .username(account.getUsername())
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
                .username(account.getUsername())
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
                .username(account.getUsername())
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

    private AuthRegisterResponse registerAppUser(AuthRegisterRequest request) {
        ensureUsernameNotExists(IdentityType.APP_USER, request.getUsername());
        AppUserAccount account = new AppUserAccount();
        account.setUserNo(authNoGenerator.userNo());
        account.setUsername(request.getUsername());
        account.setNickname(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus("ENABLED");
        appUserAccountMapper.insert(account);
        return AuthRegisterResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.APP_USER.name())
                .role(RoleType.ROLE_USER.getCode())
                .username(account.getUsername())
                .displayName(account.getNickname())
                .build();
    }

    private AuthRegisterResponse registerMerchant(AuthRegisterRequest request) {
        ensureUsernameNotExists(IdentityType.MERCHANT, request.getUsername());
        MerchantAccount account = new MerchantAccount();
        account.setMerchantNo(authNoGenerator.merchantNo());
        account.setUsername(request.getUsername());
        account.setName(request.getUsername());
        account.setContactName(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus("ENABLED");
        merchantAccountMapper.insert(account);
        return AuthRegisterResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.MERCHANT.name())
                .role(RoleType.ROLE_MERCHANT.getCode())
                .username(account.getUsername())
                .displayName(account.getName())
                .build();
    }

    private AuthRegisterResponse registerRider(AuthRegisterRequest request) {
        ensureUsernameNotExists(IdentityType.RIDER, request.getUsername());
        RiderAccount account = new RiderAccount();
        account.setRiderNo(authNoGenerator.riderNo());
        account.setUsername(request.getUsername());
        account.setName(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus("ENABLED");
        riderAccountMapper.insert(account);
        return AuthRegisterResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.RIDER.name())
                .role(RoleType.ROLE_RIDER.getCode())
                .username(account.getUsername())
                .displayName(account.getName())
                .build();
    }

    private AuthRegisterResponse registerPlatform(AuthRegisterRequest request) {
        ensureUsernameNotExists(IdentityType.PLATFORM, request.getUsername());
        PlatformAccount account = new PlatformAccount();
        account.setUsername(request.getUsername());
        account.setDisplayName(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus("ENABLED");
        platformAccountMapper.insert(account);
        return AuthRegisterResponse.builder()
                .id(account.getId())
                .identityType(IdentityType.PLATFORM.name())
                .role(RoleType.ROLE_PLATFORM.getCode())
                .username(account.getUsername())
                .displayName(account.getDisplayName())
                .build();
    }

    private void ensureUsernameNotExists(IdentityType identityType, String username) {
        boolean exists = switch (identityType) {
            case APP_USER -> appUserAccountMapper.selectCount(new LambdaQueryWrapper<AppUserAccount>()
                    .eq(AppUserAccount::getUsername, username)) > 0;
            case MERCHANT -> merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>()
                    .eq(MerchantAccount::getUsername, username)) > 0;
            case RIDER -> riderAccountMapper.selectCount(new LambdaQueryWrapper<RiderAccount>()
                    .eq(RiderAccount::getUsername, username)) > 0;
            case PLATFORM -> platformAccountMapper.selectCount(new LambdaQueryWrapper<PlatformAccount>()
                    .eq(PlatformAccount::getUsername, username)) > 0;
        };
        if (exists) {
            throw new BizException(ErrorCode.AUTH_USERNAME_EXISTS);
        }
    }
}
