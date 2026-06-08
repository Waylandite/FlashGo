package com.flashgo.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("merchant")
public class MerchantAccount {

    @TableId
    private Long id;
    private String merchantNo;
    private String name;
    private String contactName;
    private String contactMobile;
    private String passwordHash;
    private String status;
}
