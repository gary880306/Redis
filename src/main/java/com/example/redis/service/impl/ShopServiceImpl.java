package com.example.redis.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.po.Shop;
import com.example.redis.mapper.ShopMapper;
import com.example.redis.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.example.redis.utils.RedisConstants.CATCH_SHOP_ID;
import static com.example.redis.utils.RedisConstants.CATCH_SHOP_TTL;
import static com.example.redis.utils.RedisConstants.CATCH_SHOP_NULL_TTL;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<Shop> queryById(Long id) {
        String shopKey = CATCH_SHOP_ID + id;
        // 1. 從 redis 查詢商店緩存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 2. 判斷是否存在 redis
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.success(shop);
        }

        // 解決緩存穿透
        if (shopJson != null) {
            // 代表是空字符串
            return Result.error("商店不存在");
        }

        // 4. 不存在，查詢資料庫
        Shop shop = getById(id);

        // 5. 判斷是否存在資料庫
        // 6. 不存在資料庫，直接返回
        if (shop == null) {
            // 將空值寫入 redis，設定時效2分鐘
            stringRedisTemplate.opsForValue().set(shopKey, "", CATCH_SHOP_NULL_TTL, TimeUnit.MINUTES);
            return Result.error("商店不存在");
        }
        // 7. 存在資料庫，寫入 redis，設定時效30分鐘
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), CATCH_SHOP_TTL, TimeUnit.MINUTES);

        // 8. 返回
        return Result.success(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Shop> update(Shop shop) {
        Long shopId = shop.getShopId();
        if (shopId == null) {
            return Result.error("商店ID不能為空");
        }
        // 1. 更新資料庫
        updateById(shop);
        // 2. 刪除 redis 緩存
        stringRedisTemplate.delete(CATCH_SHOP_ID + shopId);
        return Result.success("更新成功", shop);
    }
}
