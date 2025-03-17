package com.example.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
public class RedisIdWorker {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 開始時間戳
    private static final long BEGIN_TIMESTAMP = 1735689600; // 20250101

    // 序列號位數
    private static final int COUNT_BITS = 32;

    public long nextId(String keyPrefix) {
        // 1. 生成時間戳記
        LocalDateTime now = LocalDateTime.now();
        long current = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = current - BEGIN_TIMESTAMP;
        // 2. 生成序列號
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 3. 拼接
        return timestamp << COUNT_BITS | count;
    }
}
