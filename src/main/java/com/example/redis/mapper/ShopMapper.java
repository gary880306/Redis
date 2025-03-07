package com.example.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.redis.entity.po.Shop;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
}
