package com.example.redis.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.po.Shop;
import com.example.redis.mapper.ShopMapper;
import com.example.redis.service.ShopService;
import com.example.redis.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<Shop> queryById(Long id) {
        String shopKey = RedisConstants.CATCH_SHOP_ID + id;
        // 1. 從 redis 查詢商店緩存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 2. 判斷是否存在 redis
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.success(shop);
        }

        // 4. 不存在，查詢資料庫
        Shop shop = getById(id);

        // 5. 判斷是否存在資料庫
        // 6. 不存在資料庫，直接返回
        if (shop == null) {
            return Result.error("商店不存在");
        }
        // 7. 存在資料庫，寫入 redis
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop));

        // 8. 返回
        return Result.success(shop);
    }
}
