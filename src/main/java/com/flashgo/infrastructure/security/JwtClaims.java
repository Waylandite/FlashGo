package com.flashgo.infrastructure.security;

import com.flashgo.auth.entity.IdentityType;
import com.flashgo.auth.entity.RoleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtClaims {

    private Long subjectId;
    private IdentityType identityType;
    private RoleType roleType;
}
