package com.example.redis.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.User;
import com.example.redis.service.SmsService;
import com.example.redis.service.UserService;
import com.example.redis.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@CrossOrigin(origins = "*") // 允許跨域請求
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final SmsService smsService;
    private final UserService userService;


    public LoginController(SmsService smsService, UserService userService) {
        this.smsService = smsService;
        this.userService = userService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testApi(@RequestHeader("Authorization") String authorization) {
        System.out.println("收到 Authorization Header：" + authorization);
        return ResponseEntity.ok("成功攜帶 Token");
    }

    @PostMapping("/code")
    public String sendCode(@RequestParam("phone") String phone) {
        // 1. 生成驗證碼
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 2. 保存驗證碼到Redis，設置過期時間為5分鐘 (前綴login:code)
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 3. 發送驗證碼（實際項目中對接短信服務）
        smsService.sendSms(phone, code);

        return "驗證碼發送成功";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        // 1. 從Redis獲取驗證碼並校驗
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);

        if (cacheCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("驗證碼已過期");
        }

        if (!cacheCode.equals(code)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("驗證碼錯誤");
        }

        // 2. 根據手機號查詢用戶 query by MyBatis-Plus with phone
        User user = userService.lambdaQuery().eq(User::getPhone, phone).one();


        // 3. 保存用戶登錄資訊
        String token = UUID.randomUUID().toString(); // 生成token(登錄令牌)
        UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDto); // 轉成 HashMap
        String tokenKey = RedisConstants.LOGIN_TOKEN_KEY + token;
        // 確保所有值都是 String
        Map<String, String> stringUserMap = new HashMap<>();
        userMap.forEach((key, value) -> {
            stringUserMap.put(key, value == null ? "" : String.valueOf(value));
        });
        stringRedisTemplate.opsForHash().putAll(tokenKey, stringUserMap);// 存入 redis
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_TOKEN_TTL, TimeUnit.MINUTES); // 設置 token 時效

        // 4. 刪除驗證碼
        stringRedisTemplate.delete("login:code:" + phone);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body("登錄成功");
    }
} 