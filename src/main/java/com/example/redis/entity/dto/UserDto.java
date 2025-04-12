package com.example.redis.entity.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String avatar;
}
