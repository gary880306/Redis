package com.example.redis.utils;

import com.example.redis.entity.dto.UserDto;

public class UserHolder {
    private static final ThreadLocal<UserDto> userThreadLocal = new ThreadLocal<>();

    // 設置當前用戶
    public static void setUser(UserDto user) {
        userThreadLocal.set(new UserDto());
    }

    // 獲取當前用戶
    public static UserDto getUser() {
        return userThreadLocal.get();
    }

    // 清除當前用戶（防止內存洩漏）
    public static void clear() {
        userThreadLocal.remove();
    }
}
