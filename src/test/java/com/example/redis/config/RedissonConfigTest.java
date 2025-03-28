package com.example.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * 可重入鎖 + 聯鎖測試
 */
@Slf4j
@SpringBootTest
public class RedissonConfigTest {

    @Qualifier("redisson")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier("redisson2")
    @Autowired
    private RedissonClient redissonClient2;

    @Qualifier("redisson3")
    @Autowired
    private RedissonClient redissonClient3;

    private RLock lock;

    @BeforeEach
    public void setUp() {
        RLock lock1 = redissonClient.getLock("order");
        RLock lock2 = redissonClient2.getLock("order");
        RLock lock3 = redissonClient3.getLock("order");

        // 創建聯鎖 multiLock
        lock = redissonClient.getMultiLock(lock1, lock2, lock3);
    }

    @Test
    void test1() throws InterruptedException {
        // 嘗試獲取鎖
        boolean isLock = lock.tryLock(1L, TimeUnit.SECONDS);
        if (!isLock) {
            log.error("獲取鎖失敗 ...... 1");
        }

        try {
            log.info("獲取鎖成功 ...... 1");
            test2();
            log.info("開始執行業務 ...... 1");
        } finally {
            log.info("準備釋放鎖 ...... 1");
            lock.unlock();
        }
    }

    @Test
    void test2() throws InterruptedException {
        // 嘗試獲取鎖
        boolean isLock = lock.tryLock(1L, TimeUnit.SECONDS);
        if (!isLock) {
            log.error("獲取鎖失敗 ...... 2");
        }

        try {
            log.info("獲取鎖成功 ...... 2");
            log.info("開始執行業務 ...... 2");
        } finally {
            log.info("準備釋放鎖 ...... 2");
            lock.unlock();
        }
    }
}
