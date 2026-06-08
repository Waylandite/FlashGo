package com.flashgo.auth.service;

import com.flashgo.auth.entity.IdentityType;
import java.util.Set;

public interface LoginStatusService {

    void activate(Long id, IdentityType identityType);

    void logout(Long id, IdentityType identityType);

    Set<String> getBannedPermissions(Long id, IdentityType identityType);

    void refresh(Long id, IdentityType identityType);
}
