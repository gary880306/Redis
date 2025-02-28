package com.example.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.redis.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
