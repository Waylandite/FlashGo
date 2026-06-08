package com.flashgo.common.context;

import com.flashgo.auth.support.AuthPrincipal;
import com.flashgo.common.enums.ErrorCode;
import com.flashgo.common.exception.BizException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class LoginContext {

    private LoginContext() {
    }

    public static AuthPrincipal current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return authPrincipal;
    }
}
