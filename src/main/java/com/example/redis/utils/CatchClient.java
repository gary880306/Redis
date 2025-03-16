package com.example.redis.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.example.redis.utils.RedisConstants.*;

@Slf4j
@Configuration
public class CatchClient {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10); // 建立一個大小為 10 的線程池

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        redisData.setData(value);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 緩存穿透
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> function, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 從 redis 查詢緩存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 判斷是否存在 redis
        if (StrUtil.isNotBlank(json)) {
            // 3. 存在，直接返回資訊
            return JSONUtil.toBean(json, type);
        }

        // 解決緩存穿透
        if (json!= null) {
            // 代表是空字符串
            return null;
        }

        // 4. 不存在，查詢資料庫
        R r = function.apply(id);

        // 5. 判斷是否存在資料庫
        // 6. 不存在資料庫，直接返回
        if (r == null) {
            // 將空值寫入 redis，設定時效2分鐘
            stringRedisTemplate.opsForValue().set(key, "", CATCH_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 7. 存在資料庫，寫入 redis，設定時效
        set(key, r, time, unit);

        // 8. 返回資訊
        return r;
    }

    // 緩存擊穿(利用邏輯過期)
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> function, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 從 redis 查詢緩存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 判斷是否存在 redis
        if (StrUtil.isBlank(json)) {
            // 3. 未存在，直接返回
            return null;
        }

        // 4. 存在，先把 json 反序列化成對象
        RedisData data = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) data.getData(), type);
        LocalDateTime expireTime = data.getExpireTime();
        // 5. 判斷是否過期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未過期，直接返回資訊
            return r;
        }
        // 5.2 已過期，緩存重建
        String lockKey = LOCK_SHOP_KEY + id;
        // 6. 緩存重建
        // 6.1 獲取互斥鎖
        boolean isLock = getLock(lockKey);
        // 6.2 判斷獲取是否成功
        if (isLock) {
            // 6.3 再次檢查是否已被其他線程更新
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                RedisData latestData = JSONUtil.toBean(json, RedisData.class);
                // 檢查是否已被更新且未過期
                if (latestData.getExpireTime().isAfter(LocalDateTime.now())) {
                    // 已被更新，直接返回
                    return JSONUtil.toBean((JSONObject) latestData.getData(), type);
                }
            }
            // 6.4 成功，開啟獨立線程處理緩存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 模擬重建緩存的延遲
                    Thread.sleep(200);
                    R r1 = function.apply(id);
                    setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 釋放鎖
                    unLock(lockKey);
                }
            });
        }

        // 6.5 返回過期的資訊
        return r;
    }

    //  獲取鎖
    private boolean getLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    // 釋放鎖
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

}
