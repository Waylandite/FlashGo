package com.flashgo.auth.service;

import com.flashgo.auth.model.dto.AuthLoginRequest;
import com.flashgo.auth.model.vo.AuthLoginResponse;
import com.flashgo.auth.model.vo.AuthMeResponse;

public interface AuthService {

    AuthLoginResponse login(AuthLoginRequest request);

    AuthMeResponse me();

    void logout();
}
