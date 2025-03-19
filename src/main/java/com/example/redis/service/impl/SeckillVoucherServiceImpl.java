package com.example.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.entity.po.SeckillVoucher;
import com.example.redis.mapper.SeckillVoucherMapper;
import com.example.redis.service.SeckillVoucherService;
import org.springframework.stereotype.Service;

@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements SeckillVoucherService {
}
