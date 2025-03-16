package com.example.redis.service.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ShopServiceImplTest {

    @Autowired
    private ShopServiceImpl shopService;

    // 緩存預熱
    @Test
    @Disabled
    void saveShopToRedis() throws InterruptedException {
        shopService.saveShopToRedis(1L, 10L);
    }
}
