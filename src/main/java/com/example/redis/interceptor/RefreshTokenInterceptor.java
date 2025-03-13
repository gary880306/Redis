package com.example.redis.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.utils.RedisConstants;
import com.example.redis.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 讀取 Authorization Header
        String token = request.getHeader("Authorization");

        // 2. 檢查 token 是否有效
        if (StrUtil.isBlank(token) || !token.startsWith("Bearer ")) {
            return true;
        }

        // 去掉 "Bearer "，只保留 token 值
        token = token.substring(7);

        // 3. Token Key
        String key = RedisConstants.LOGIN_TOKEN_KEY + token;

        // 4. 查詢 Redis，檢查 Token 是否存在
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {
            return true;
        }

        // 5. 解析用戶資訊
        UserDto userDto = BeanUtil.fillBeanWithMap(userMap, new UserDto(), false);

        // 6. 保存用戶資訊到 ThreadLocal
        UserHolder.setUser(userDto);

        // 7. 刷新 Token 過期時間
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_TOKEN_TTL, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.clear();
    }

}
