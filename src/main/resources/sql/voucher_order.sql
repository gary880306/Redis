-- 優惠券訂單表
CREATE TABLE `voucher_order` (
                                    `id` bigint(20) NOT NULL COMMENT '主鍵',
                                    `user_id` bigint(20) unsigned NOT NULL COMMENT '下單的用戶id',
                                    `voucher_id` bigint(20) unsigned NOT NULL COMMENT '購買的代金券id',
                                    `pay_type` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '支付方式 1: 餘額支付; 2: LINE; 3: 信用卡',
                                    `status` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '訂單狀態, 1: 未支付; 2: 已支付; 3: 已核銷; 4: 已取消; 5: 已過期',
                                    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下單時間',
                                    `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付時間',
                                    `use_time` timestamp NULL DEFAULT NULL COMMENT '核銷時間',
                                    `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款時間',
                                    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;