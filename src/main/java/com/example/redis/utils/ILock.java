package com.example.redis.utils;

public interface ILock {

    /**
     * 獲取鎖
     * @param timeoutSec 鎖的超時時間，過期自動釋放
     * @return true 獲取成功，false 獲取失敗
     */
    boolean tryLock(long timeoutSec);

    /**
     * 釋放鎖
     */
    void unlock();

}
