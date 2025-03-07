package com.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.redis.common.Result;
import com.example.redis.entity.po.Shop;

public interface ShopService extends IService<Shop> {

    Result<Shop> queryById(Long id);

}
