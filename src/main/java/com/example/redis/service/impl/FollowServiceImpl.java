package com.example.redis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Follow;
import com.example.redis.mapper.FollowMapper;
import com.example.redis.service.FollowService;
import com.example.redis.service.UserService;
import com.example.redis.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Result<String> follow(Long followUserId, Boolean isFollow) {
        // 1. 獲取登入用戶
        Long userId = UserHolder.getUser().getUserId();
        String key = "follows:" + userId;
        // 2. 判斷關注還是取消
        if (isFollow) {
            // 3. 關注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // sadd userId followUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 4. 取消 delete from follow where user_id = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                // remove userId followUserId
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.success("成功!");
    }

    @Override
    public Result<Boolean> isFollow(Long followUserId) {
        // 1. 獲取登入用戶
        Long userId = UserHolder.getUser().getUserId();
        Long count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        return Result.success(count > 0);
    }

    @Override
    public Result<List<UserDto>> followCommons(Long id) {
        // 1. 獲取登入用戶
        Long userId = UserHolder.getUser().getUserId();
        String key = "follows:" + userId;

        // 2. 求交集
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);

        // 3. 無交集，返回空集合
        if (intersect == null || intersect.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 4. 解析id集合
        Collection<Long> ids = intersect.stream().map(Long::valueOf).toList();

        // 5. 查詢用戶
        List<UserDto> users = userService.listByIds(ids).stream().map(user -> BeanUtil.copyProperties(user, UserDto.class)).toList();

        return Result.success(users);
    }

}