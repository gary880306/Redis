package com.example.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redisson() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 創建對象
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redisson2() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6380");
        // 創建對象
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redisson3() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6381");
        // 創建對象
        return Redisson.create(config);
    }
}
