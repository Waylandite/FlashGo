package com.flashgo.auth.service;

import com.flashgo.auth.model.dto.AuthLoginRequest;
import com.flashgo.auth.model.dto.AuthRegisterRequest;
import com.flashgo.auth.model.vo.AuthLoginResponse;
import com.flashgo.auth.model.vo.AuthMeResponse;
import com.flashgo.auth.model.vo.AuthRegisterResponse;

public interface AuthService {

    AuthRegisterResponse register(AuthRegisterRequest request);

    AuthLoginResponse login(AuthLoginRequest request);

    AuthMeResponse me();

    void logout();
}
