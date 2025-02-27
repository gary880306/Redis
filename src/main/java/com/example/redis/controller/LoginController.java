package com.example.redis.controller;

import com.example.redis.service.SmsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.example.redis.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.example.redis.utils.RedisConstants.LOGIN_CODE_TTL;

@Slf4j
@CrossOrigin(origins = "*") // 允許跨域請求
@RestController
@RequestMapping("/user")
public class LoginController {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsService smsService;
    
    public LoginController(RedisTemplate<String, Object> redisTemplate, SmsService smsService) {
        this.redisTemplate = redisTemplate;
        this.smsService = smsService;
    }

    @PostMapping("/code")
    public String sendCode(@RequestParam("phone") String phone) {
        // 1. 生成驗證碼
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 2. 保存驗證碼到Redis，設置過期時間為5分鐘 (前綴login:code)
        redisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        
        // 3. 發送驗證碼（實際項目中對接短信服務）
        smsService.sendSms(phone, code);
        
        return "驗證碼發送成功";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        // 1. 從Redis獲取驗證碼並校驗
        String cacheCode = (String) redisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        
        if (cacheCode == null) {
            return "驗證碼已過期";
        }
        
        if (!cacheCode.equals(code)) {
            return "驗證碼錯誤";
        }
        // 2. 根據手機號查詢用戶


        // 2. 生成登錄令牌
        String token = java.util.UUID.randomUUID().toString();
        
        // 3. 保存用戶登錄狀態，設置30分鐘過期
        redisTemplate.opsForValue().set("login:token:" + token, phone, 30, TimeUnit.MINUTES);
        
        // 4. 刪除驗證碼
        redisTemplate.delete("login:code:" + phone);
        
        return "登錄成功，token: " + token;
    }
} 