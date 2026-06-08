package com.flashgo.auth.support;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class SecurityAuthorityBuilder {

    private SecurityAuthorityBuilder() {
    }

    public static Collection<? extends GrantedAuthority> build(String roleCode, Set<String> permissions) {
        Set<String> authorities = new LinkedHashSet<>();
        authorities.add(roleCode);
        if (permissions != null) {
            authorities.addAll(permissions);
        }
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
