package com.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.redis.common.Result;
import com.example.redis.entity.po.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {


    Result<Long> seckillVoucher(Long voucherId);
}