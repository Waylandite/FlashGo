package com.flashgo.auth.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthLoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String identityType;
    private String role;
    private AuthMeResponse userInfo;
}
