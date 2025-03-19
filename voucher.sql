CREATE TABLE `voucher` (
                              `voucher_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '優惠券ID',
                              `shop_id` bigint(20) unsigned DEFAULT NULL COMMENT '商店ID',
                              `title` varchar(255) NOT NULL NULL COMMENT '優惠券名稱',
                              `sub_title` varchar(255) DEFAULT NULL COMMENT '名稱',
                              `rules` varchar(1024) DEFAULT NULL COMMENT '使用規則',
                              `pay_value` bigint(10) unsigned NOT NULL NULL COMMENT '支付金額',
                              `actual_value` bigint(10) NOT NULL NULL COMMENT '折扣金額',
                              `type` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0,普通券; 1,秒殺券',
                              `status` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '1,上架; 2,下架; 3,過期',
                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
                              `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                              PRIMARY KEY (`voucher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;


ALTER TABLE voucher AUTO_INCREMENT = 1;
-- fakeData
INSERT INTO `voucher`
(`shop_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`)
VALUES
    (1, '限時秒殺8折券', '每日限量50張', '使用規則：
1. 僅限當日使用
2. 不可與其他優惠同時使用
3. 每人限領一張
4. 全場商品皆可使用', 0, 500, 1, 1);