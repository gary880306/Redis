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

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.redis.utils.RedisConstants.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final SmsService smsService;
    private final UserService userService;

    // 生成驗證碼
    private final SecureRandom secureRandom = new SecureRandom();

    // 驗證碼頻率 60s
    private static final long CODE_REQUEST_LIMIT = 60;


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
    public ResponseEntity<?> sendCode(@RequestParam("phone") String phone) {
        // 1. 檢查該手機號碼的請求頻率
        String requestKey = REQUEST_LIMIT_KEY + phone;
        String lastRequestTime = stringRedisTemplate.opsForValue().get(requestKey);
        long currentTime = System.currentTimeMillis() / 1000;

        if (lastRequestTime != null) {
            long timeDiff = currentTime - Long.parseLong(lastRequestTime);
            if (timeDiff < CODE_REQUEST_LIMIT) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("請求過於頻繁，請在" + (CODE_REQUEST_LIMIT - timeDiff) + "秒後再試");
            }
        }

        // 2. 保存本次請求時間到 Redis，設置過期時間為60秒
        stringRedisTemplate.opsForValue().set(requestKey, String.valueOf(currentTime), CODE_REQUEST_LIMIT, TimeUnit.SECONDS);

        // 3. 生成 6 位數字驗證碼
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        // 4. 保存驗證碼到 Redis，設置過期時間為5分鐘
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5. 發送驗證碼
        smsService.sendSms(phone, code);

        log.info("向手機號 {} 發送驗證碼: {}", phone, code);
        return ResponseEntity.ok("驗證碼發送成功");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        // 1. 從 Redis 獲取驗證碼並驗證
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);

        if (cacheCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("驗證碼已過期");
        }

        if (!cacheCode.equals(code)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("驗證碼錯誤");
        }

        // 2. 根據手機號查詢用戶 query by MyBatis-Plus with phone
        User user = userService.lambdaQuery().eq(User::getPhone, phone).one();

        // 如果用戶不存在，直接返回
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用戶不存在");
        }

        // 3. 設置用戶登錄資訊
        String token = UUID.randomUUID().toString(); // 生成token
        UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDto); // 轉成 HashMap
        String tokenKey = RedisConstants.LOGIN_TOKEN_KEY + token;

        // 確保所有值都是 String
        Map<String, String> stringUserMap = new HashMap<>();
        userMap.forEach((key, value) -> {
            stringUserMap.put(key, value == null ? "" : String.valueOf(value));
        });

        // 4. 保存用戶資訊到 Redis
        stringRedisTemplate.opsForHash().putAll(tokenKey, stringUserMap);

        // 5. 設置 token 過期時間為30分鐘
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_TOKEN_TTL, TimeUnit.MINUTES);

        // 6. 刪除 Redis 中的驗證碼
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body("登錄成功");
    }
} 