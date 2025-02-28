package com.example.redis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("user")
public class User implements Serializable {

    @TableId(value = "user_id")
    // 用戶唯一識別碼
    private Long userId;
    // 用戶名稱（顯示用戶名）
    private String username;
    // 電子郵件地址
    private String email;
    // 密碼（建議使用加密方式存儲）
    private String password;
    // 聯絡電話
    private String phone;
    // 寄送地址
    private String address;
    // 註冊日期
    private Timestamp registrationDate;
} 