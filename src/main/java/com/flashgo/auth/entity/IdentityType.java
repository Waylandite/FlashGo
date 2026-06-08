package com.flashgo.auth.entity;

import com.flashgo.common.enums.ErrorCode;
import com.flashgo.common.exception.BizException;
import java.util.Arrays;

public enum IdentityType {

    APP_USER,
    MERCHANT,
    RIDER,
    PLATFORM;


    public static IdentityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID.getCode(), "无效的登录类型"));
    }
}
