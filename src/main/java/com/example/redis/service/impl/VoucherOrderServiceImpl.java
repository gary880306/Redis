package com.example.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.po.SeckillVoucher;
import com.example.redis.entity.po.VoucherOrder;
import com.example.redis.mapper.VoucherOrderMapper;
import com.example.redis.service.SeckillVoucherService;
import com.example.redis.service.VoucherOrderService;
import com.example.redis.service.VoucherService;
import com.example.redis.utils.RedisIdWorker;
import com.example.redis.utils.SimpleRedisLock;
import com.example.redis.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private RedissonClient redissonClient;

    @Override
    public Result<Long> seckillVoucher(Long voucherId) {
        // 1. 查詢優惠券
        SeckillVoucher sv = seckillVoucherService.getById(voucherId);

        // 2. 判斷秒殺是否開始或結束
        if (sv.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.error("秒殺尚未開始!");
        }
        if (sv.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.error("秒殺已經結束!");
        }

        // 3. 判斷庫存是否充足
        if (sv.getStock() < 1) {
            return Result.error("庫存不足!");
        }

//        Long userId = UserHolder.getUser().getUserId();
        // TODO: TEST USERID
        Long userId = 10L;

        // 創建分布式鎖
//        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);

        // 獲取鎖
        boolean isLock = lock.tryLock();

        if (!isLock) {
            return Result.error("不允許重複下單!");
        }

        try {
            // 獲取代理對象 (事務)
            VoucherOrderService proxy = (VoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            // 釋放鎖
            lock.unlock();
        }

    }

    @Transactional
    public Result<Long> createVoucherOrder(Long voucherId) {
        // 4. 一人一單
//        Long count = query().eq("user_id", UserHolder.getUser().getUserId()).eq("voucher_id", voucherId).count();
        // TODO: TEST USERID
        Long count = query().eq("user_id", 10L).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.error("用戶已經購買過一次!");
        }

        // 5. 扣減庫存
        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherId).gt("stock", 0).update();

        if (!success) {
            return Result.error("庫存不足!");
        }

        // 6. 創建訂單
        long orderId = redisIdWorker.nextId("order");
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
//        voucherOrder.setUserId(UserHolder.getUser().getUserId());
        // TODO: TEST USERID
        voucherOrder.setUserId(10L);
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);

        // 7. 返回訂單ID
        return Result.success(orderId);
    }
}