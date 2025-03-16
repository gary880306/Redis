package com.example.redis.service.impl;

import com.example.redis.entity.po.Shop;
import com.example.redis.utils.CatchClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static com.example.redis.utils.RedisConstants.CATCH_SHOP_KEY;

@SpringBootTest
public class ShopServiceImplTest {

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private CatchClient catchClient;

    // 緩存預熱
    @Test
    @Disabled
    void saveShopToRedis() throws InterruptedException {
        Shop shop = shopService.getById(1L);
        catchClient.setWithLogicalExpire(CATCH_SHOP_KEY + 1L, shop, 10L, TimeUnit.SECONDS);
    }
}
