CREATE TABLE `follow` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                             `user_id` bigint(20) unsigned NOT NULL COMMENT '用戶ID',
                             `follow_user_id` bigint(20) unsigned NOT NULL COMMENT '關聯的用戶ID',
                             `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;