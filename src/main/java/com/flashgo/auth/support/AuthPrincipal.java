package com.flashgo.auth.support;

import com.flashgo.auth.entity.IdentityType;
import com.flashgo.auth.entity.RoleType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Builder
public class AuthPrincipal implements Serializable {

    private Long id;
    private IdentityType identityType;
    private RoleType roleType;
    private String username;
    private String displayName;
    private Set<String> permissions;

    public Collection<? extends GrantedAuthority> toAuthorities() {
        return SecurityAuthorityBuilder.build(roleType.getCode(), permissions);
    }
}
