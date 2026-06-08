package com.flashgo.auth.entity;

import java.util.Arrays;

public enum RoleType {

    ROLE_USER(IdentityType.APP_USER),
    ROLE_MERCHANT(IdentityType.MERCHANT),
    ROLE_RIDER(IdentityType.RIDER),
    ROLE_PLATFORM(IdentityType.PLATFORM);

    private final IdentityType identityType;

    RoleType(IdentityType identityType) {
        this.identityType = identityType;
    }

    public String getCode() {
        return name();
    }

    public IdentityType getIdentityType() {
        return identityType;
    }

    public static RoleType fromIdentityType(IdentityType identityType) {
        return Arrays.stream(values())
                .filter(item -> item.identityType == identityType)
                .findFirst()
                .orElseThrow();
    }
}
