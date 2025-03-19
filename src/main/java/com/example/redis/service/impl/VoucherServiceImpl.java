package com.example.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.entity.po.Voucher;
import com.example.redis.mapper.VoucherMapper;
import com.example.redis.service.VoucherService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        // 查詢指定店鋪的優惠券
        return lambdaQuery()
                .eq(Voucher::getShopId, shopId)
                .orderByDesc(Voucher::getCreateTime)
                .list();
    }

}
