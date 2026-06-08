package com.flashgo.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthLoginRequest {

    @NotBlank(message = "登录类型不能为空")
    private String type;

    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;
}
