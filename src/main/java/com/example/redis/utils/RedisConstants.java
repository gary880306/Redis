package com.example.redis.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 5L;
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final Long LOGIN_TOKEN_TTL = 30L;
    public static final String REQUEST_LIMIT_KEY = "request:limit:";
    public static final String CATCH_SHOP_ID = "catch:shop:";
    public static final Long CATCH_SHOP_NULL_TTL = 2L;
    public static final Long CATCH_SHOP_TTL = 30L;
}
