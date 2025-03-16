package com.example.redis.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {

    private LocalDateTime expireTime; // 邏輯過期時間

    private Object data; // 存儲的數據對象
}
