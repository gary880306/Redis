-- KEYS[1] 就是鎖的 KEY, ARGV[1] 就是當前線程標誌

-- 獲取鎖中的標誌，判斷是否與當前線程標誌相同
if (redis.call('get', KEYS[1]) == ARGV[1]) then
    -- 相同，刪除鎖
    return redis.call('DEL', KEYS[1])
end
    --  不相同，直接返回
return 0