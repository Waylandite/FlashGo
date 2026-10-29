package com.flashgo.auth.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthRegisterResponse {

    private Long id;
    private String identityType;
    private String role;
    private String username;
    private String displayName;
}
