package com.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.redis.common.Result;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Follow;

import java.util.List;

public interface FollowService extends IService<Follow> {

    Result<String> follow(Long followUserId, Boolean isFollow);

    Result<Boolean> isFollow(Long followUserId);

    Result<List<UserDto>> followCommons(Long id);
}
