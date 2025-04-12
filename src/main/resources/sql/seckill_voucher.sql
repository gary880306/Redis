CREATE TABLE `seckill_voucher` (
                                      `voucher_id` bigint(20) unsigned NOT NULL COMMENT '優惠券ID',
                                      `stock` int(8) unsigned NOT NULL NULL COMMENT '庫存',
                                      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
                                      `begin_time` timestamp NOT NULL COMMENT '生效時間',
                                      `end_time` timestamp NOT NULL COMMENT '失效時間',
                                      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                      PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒殺優惠券表，與優惠券是一對一關係';




-- fakeData
INSERT INTO `seckill_voucher`
(`voucher_id`, `stock`, `begin_time`, `end_time`)
VALUES
    (1, 50, '2025-03-20 10:00:00', '2025-03-20 22:00:00');

-- 測試超賣 stock 可以存入負數
ALTER TABLE `seckill_voucher`
    MODIFY COLUMN `stock` int(8) NOT NULL COMMENT '庫存';