package com.example.redis.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsService {
    public void sendSms(String phone, String code) {
        // 這裡模擬發送短信
        log.info("向手機號 {} 發送驗證碼: {}", phone, code);
    }
} 