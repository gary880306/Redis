package com.example.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.entity.dto.VoucherDto;
import com.example.redis.entity.po.SeckillVoucher;
import com.example.redis.entity.po.Voucher;
import com.example.redis.mapper.VoucherMapper;
import com.example.redis.service.SeckillVoucherService;
import com.example.redis.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.redis.utils.RedisConstants.SECKILL_STOCK_KEY;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    @Autowired
    private SeckillVoucherService seckillVoucherService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        // 查詢指定店鋪的優惠券
        return lambdaQuery()
                .eq(Voucher::getShopId, shopId)
                .orderByDesc(Voucher::getCreateTime)
                .list();
    }

    @Override
    @Transactional
    public Long addSeckillVoucher(VoucherDto voucherDto) {
        // 保存優惠券
        Voucher voucher = new Voucher();
        voucher.setShopId(voucherDto.getShopId());
        voucher.setTitle(voucherDto.getTitle());
        voucher.setSubTitle(voucherDto.getSubTitle());
        voucher.setRules(voucherDto.getRules());
        voucher.setPayValue(voucherDto.getPayValue());
        voucher.setActualValue(voucherDto.getActualValue());
        voucher.setType(voucherDto.getType());
        voucher.setStatus(voucherDto.getStatus());
        save(voucher);

        // 保存秒殺資訊
        Long voucherId = voucher.getId();
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucherId);
        seckillVoucher.setStock(voucherDto.getStock());
        seckillVoucher.setBeginTime(voucherDto.getBeginTime());
        seckillVoucher.setEndTime(voucherDto.getEndTime());
        seckillVoucherService.save(seckillVoucher);

        // 保存秒殺庫存到 redis 中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucherId, voucherDto.getStock().toString());

        return voucherId;
    }

}
