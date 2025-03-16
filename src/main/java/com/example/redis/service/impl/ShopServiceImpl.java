package com.example.redis.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.po.Shop;
import com.example.redis.mapper.ShopMapper;
import com.example.redis.service.ShopService;
import com.example.redis.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.redis.utils.RedisConstants.CATCH_SHOP_ID;
import static com.example.redis.utils.RedisConstants.CATCH_SHOP_TTL;
import static com.example.redis.utils.RedisConstants.CATCH_SHOP_NULL_TTL;
import static com.example.redis.utils.RedisConstants.LOCK_SHOP_KEY;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10); // 建立一個大小為 10 的線程池

    @Override
    public Result<Shop> queryById(Long id) {
        // 緩存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥鎖解決緩存擊穿
//        Shop shop = queryWithMutex(id);

        // 邏輯過期解決緩存擊穿
        Shop shop = queryWithLogicalExpire(id);

        if (shop == null) {
            return Result.error("商店不存在");
        }
        return Result.success(shop);
    }

    // 緩存穿透
    public Shop queryWithPassThrough(Long id) {
        String shopKey = CATCH_SHOP_ID + id;
        // 1. 從 redis 查詢商店緩存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 2. 判斷是否存在 redis
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 解決緩存穿透
        if (shopJson != null) {
            // 代表是空字符串
            return null;
        }

        // 4. 不存在，查詢資料庫
        Shop shop = getById(id);

        // 5. 判斷是否存在資料庫
        // 6. 不存在資料庫，直接返回
        if (shop == null) {
            // 將空值寫入 redis，設定時效2分鐘
            stringRedisTemplate.opsForValue().set(shopKey, "", CATCH_SHOP_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 7. 存在資料庫，寫入 redis，設定時效30分鐘
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), CATCH_SHOP_TTL, TimeUnit.MINUTES);

        // 8. 返回
        return shop;
    }

    // 緩存擊穿(利用互斥鎖)
    public Shop queryWithMutex(Long id) {
        String shopKey = CATCH_SHOP_ID + id;
        // 1. 從 redis 查詢商店緩存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 2. 判斷是否存在 redis
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 解決緩存穿透
        if (shopJson != null) {
            // 代表是空字符串
            return null;
        }

        // 4. 實現緩存重建
        Shop shop = null;
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = false;

        try {
            // 4.1 獲取互斥鎖
            isLock = getLock(lockKey);

            // 4.2 判斷是否獲取成功
            if (!isLock) {
                // 4.3 獲取鎖失敗，休眠一段時間後重試
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            // 4.4 獲取鎖成功後，再次檢查緩存（雙重檢查，防止其他線程已經重建了緩存）
            shopJson = stringRedisTemplate.opsForValue().get(shopKey);
            if (StrUtil.isNotBlank(shopJson)) {
                return JSONUtil.toBean(shopJson, Shop.class);
            }

            // 4.5 緩存仍然不存在，查詢資料庫
            shop = getById(id);

            // 模擬重建緩存的延遲
            Thread.sleep(200);

            // 4.6 判斷是否存在資料庫
            if (shop == null) {
                // 將空值寫入 redis，設定時效2分鐘
                stringRedisTemplate.opsForValue().set(shopKey, "", CATCH_SHOP_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }

            // 4.7 存在資料庫，寫入 redis，設定時效30分鐘
            stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), CATCH_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("緩存重建異常", e);
        } finally {
            // 5. 釋放鎖，只有獲取到鎖的線程才需要釋放
            if (isLock) {
                unLock(lockKey);
            }
        }

        // 6. 返回結果
        return shop;
    }

    // 緩存擊穿(利用邏輯過期)
    public Shop queryWithLogicalExpire(Long id) {
        String shopKey = CATCH_SHOP_ID + id;
        // 1. 從 redis 查詢商店緩存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 2. 判斷是否存在 redis
        if (StrUtil.isBlank(shopJson)) {
            // 3. 未存在，直接返回
            return null;
        }

        // 4. 存在，先把 json 反序列化成對象
        RedisData data = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) data.getData(), Shop.class);
        LocalDateTime expireTime = data.getExpireTime();
        // 5. 判斷是否過期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未過期，直接返回商店資訊
            return shop;
        }
        // 5.2 已過期，緩存重建
        String lockKey = LOCK_SHOP_KEY + id;
        // 6. 緩存重建
        // 6.1 獲取互斥鎖
        boolean isLock = getLock(lockKey);
        // 6.2 判斷獲取是否成功
        if (isLock) {
            // 6.3 再次檢查是否已被其他線程更新
            shopJson = stringRedisTemplate.opsForValue().get(shopKey);
            if (StrUtil.isNotBlank(shopJson)) {
                RedisData latestData = JSONUtil.toBean(shopJson, RedisData.class);
                // 檢查是否已被更新且未過期
                if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
                    // 已被更新，直接返回
                    return JSONUtil.toBean((JSONObject) latestData.getData(), Shop.class);
                }
            }
            // 6.4 成功，開啟獨立線程處理緩存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    this.saveShopToRedis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 釋放鎖
                    unLock(lockKey);
                }
            });
        }

        // 6.5 返回過期的商店資訊
        return shop;
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

    //  獲取鎖
    private boolean getLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    // 釋放鎖
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    // 緩存預熱
    public void saveShopToRedis(Long id, Long expireTime) throws InterruptedException {
        // 1. 查詢商店資訊
        Shop shop = getById(id);

        // 模擬重建緩存的延遲
        Thread.sleep(200L);

        // 2. 封裝 RedisData
        RedisData data = new RedisData();
        data.setData(shop);
        data.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));

        // 3. 存入 Redis
        stringRedisTemplate.opsForValue().set(CATCH_SHOP_ID + id, JSONUtil.toJsonStr(data));
    }
}
