package com.example.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.po.Follow;
import com.example.redis.mapper.FollowMapper;
import com.example.redis.service.FollowService;
import org.springframework.stereotype.Service;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Override
    public Result<String> follow(Long followUserId, Boolean isFollow) {
        // 1. 獲取登入用戶
        // Long userId = UserHolder.getUser().getUserId();
        // TODO:TEST USER
        Long userId = 2L;
        // 2. 判斷關注還是取消
        if (isFollow) {
            // 3. 關注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
        } else {
            // 4. 取消 delete from follow where user_id = ? and follow_user_id = ?
            remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", followUserId));
        }
        return Result.success("成功!");
    }

    @Override
    public Result<Boolean> isFollow(Long followUserId) {
        // 1. 獲取登入用戶
        // Long userId = UserHolder.getUser().getUserId();
        // TODO:TEST USER
        Long userId = 2L;
        // 2. 查詢是否關注 select count(*) from follow where user_id = ? and follow_user_id = ?
        Long count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        return Result.success(count > 0);
    }

}