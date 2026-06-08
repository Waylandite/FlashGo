package com.flashgo.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("platform_account")
public class PlatformAccount {

    @TableId
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String status;
}
