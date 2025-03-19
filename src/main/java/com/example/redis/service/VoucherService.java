package com.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.redis.entity.po.Voucher;

import java.util.List;

public interface VoucherService extends IService<Voucher> {
    /**
     * 查詢商店的優惠券列表
     */
    List<Voucher> queryVoucherOfShop(Long shopId);
}
