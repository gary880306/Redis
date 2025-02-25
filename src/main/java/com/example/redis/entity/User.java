package com.example.redis.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class User implements Serializable {
    private Long id;
    private String phone;
    private String nickname;

} 