package com.flashgo.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("app_user")
public class AppUserAccount {

    @TableId
    private Long id;
    private String userNo;
    private String nickname;
    private String mobile;
    private String passwordHash;
    private String status;
}
