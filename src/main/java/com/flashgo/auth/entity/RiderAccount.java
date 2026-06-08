package com.flashgo.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("rider")
public class RiderAccount {

    @TableId
    private Long id;
    private String riderNo;
    private String name;
    private String mobile;
    private String passwordHash;
    private String status;
}
