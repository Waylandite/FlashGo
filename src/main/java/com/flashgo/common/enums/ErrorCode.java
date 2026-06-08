package com.flashgo.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAM_INVALID("PARAM_INVALID", "参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未登录或登录已失效"),
    FORBIDDEN("FORBIDDEN", "无权限访问该资源"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在"),
    ORDER_STATUS_INVALID("ORDER_STATUS_INVALID", "当前订单状态不允许执行该操作"),
    AUTH_ACCOUNT_DISABLED("AUTH_ACCOUNT_DISABLED", "账号已禁用"),
    AUTH_PASSWORD_INVALID("AUTH_PASSWORD_INVALID", "账号或密码错误"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", "登录已过期"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
