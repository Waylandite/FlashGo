package com.flashgo.infrastructure.security;

import com.flashgo.auth.entity.RoleType;
import com.flashgo.auth.service.LoginStatusService;
import com.flashgo.auth.service.PermissionPoolService;
import com.flashgo.auth.support.AuthPrincipal;
import com.flashgo.common.enums.ErrorCode;
import com.flashgo.common.exception.BizException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final LoginStatusService loginStatusService;
    private final PermissionPoolService permissionPoolService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtClaims claims = jwtTokenService.parseToken(authHeader.substring(7));
            loginStatusService.refresh(claims.getSubjectId(), claims.getIdentityType());
            Set<String> permissions = buildPermissions(claims.getRoleType(), claims.getSubjectId(), claims.getIdentityType());
            AuthPrincipal principal = AuthPrincipal.builder()
                    .id(claims.getSubjectId())
                    .identityType(claims.getIdentityType())
                    .roleType(claims.getRoleType())
                    .username(String.valueOf(claims.getSubjectId()))
                    .displayName(String.valueOf(claims.getSubjectId()))
                    .permissions(permissions)
                    .build();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.toAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (BizException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(ErrorCode.FORBIDDEN.getCode().equals(ex.getCode()) ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"" + ex.getCode() + "\",\"message\":\"" + ex.getMessage() + "\",\"data\":null}");
        }
    }

    private Set<String> buildPermissions(RoleType roleType, Long subjectId, com.flashgo.auth.entity.IdentityType identityType) {
        Set<String> permissions = new HashSet<>(permissionPoolService.getPermissions(roleType));
        permissions.removeAll(loginStatusService.getBannedPermissions(subjectId, identityType));
        return permissions;
    }
}
