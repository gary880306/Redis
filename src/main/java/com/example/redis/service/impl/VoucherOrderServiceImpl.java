package com.example.redis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.po.VoucherOrder;
import com.example.redis.mapper.VoucherOrderMapper;
import com.example.redis.service.SeckillVoucherService;
import com.example.redis.service.VoucherOrderService;
import com.example.redis.utils.RedisIdWorker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Autowired
    private SeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    @Qualifier("redisson")
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private VoucherOrderService proxy;

    @Override
    public Result<Long> seckillVoucher(Long voucherId) {
        //  1. 獲取用戶 ID
        // Long userId = UserHolder.getUser().getUserId();
        // TODO: TEST USERID
        Long userId = 1L;
        // 2. 獲取訂單 ID
        long orderId = redisIdWorker.nextId("order");
        // 3. 執行 Lua 腳本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(),
                String.valueOf(orderId)
        );

        // 4. 判斷是否為0
        int r = result.intValue();
        if (r != 0) {
            // 4.1 不為0，代表沒有資格購買
            return Result.error(r == 1 ? "庫存不足!" : "不能重複下單!");
        }

        // 5. 獲取代理對象 (事務)
        proxy = (VoucherOrderService) AopContext.currentProxy();

        // 6. 返回訂單id
        return Result.success(orderId);

    }

    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";

        @Override
        public void run() {
            while (true) {
                try {
                    // 1. 獲取消息隊列中的訂單資訊 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );

                    // 2. 判斷消息獲取是否成功
                    if (list == null || list.isEmpty()) {
                        // 2.1 獲取失敗，代表沒有消息，繼續下一次循環
                        continue;
                    }

                    // 3. 解析消息中的資訊
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);

                    // 4. 下單
                    handleVoucherOrder(voucherOrder);

                    // 5. ACK 確認 SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("處理訂單異常", e);
                    // 處理異常失敗的 pending-list
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    // 1. 獲取 pending-list 中的訂單資訊 XREADGROUP GROUP g1 c1 COUNT 1 STREAMS stream.orders 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    // 2. 判斷消息獲取是否成功
                    if (list == null || list.isEmpty()) {
                        // 2.1 獲取失敗，代表 pending-list 中沒有異常消息，結束循環
                        break;
                    }

                    // 3. 解析消息中的資訊
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);

                    // 4. 下單
                    handleVoucherOrder(voucherOrder);

                    // 5. ACK 確認 SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("處理訂單異常", e);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        }
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        // 創建分布式鎖
        // 1. 獲取用戶
        Long userId = voucherOrder.getUserId();

        // 2. 創建鎖對象
        RLock lock = redissonClient.getLock("lock:order:" + userId);

        // 3. 獲取鎖
        boolean isLock = lock.tryLock();

        // 4. 判斷鎖是否獲取成功
        if (!isLock) {
            // 4.1 獲取鎖失敗
            log.error("不允許重複下單!");
            return;
        }

        try {
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            // 釋放鎖
            lock.unlock();
        }
    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 5. 一人一單
        // Long count = query().eq("user_id", UserHolder.getUser().getUserId()).eq("voucher_id", voucherId).count();
        // TODO: TEST USERID
        Long count = query().eq("user_id", 10L).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            log.error("用戶已經購買過一次！");
            return;
        }

        // 6. 扣減庫存
        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0).update();

        if (!success) {
            log.error("庫存不足！");
            return;
        }

        // 7. 創建訂單
        save(voucherOrder);
    }

}