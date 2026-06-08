package com.flashgo.auth.service;

import com.flashgo.auth.entity.RoleType;
import java.util.Set;

public interface PermissionPoolService {

    Set<String> getPermissions(RoleType roleType);
}
