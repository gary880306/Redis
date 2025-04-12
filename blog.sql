CREATE TABLE `blog` (
                           `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                           `shop_id` bigint(20) NOT NULL COMMENT '商店ID',
                           `user_id` bigint(20) unsigned NOT NULL COMMENT '用戶ID',
                           `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '標題',
                           `images` varchar(2048)  COMMENT '探店的照片，最多9張，多張以","隔開',
                           `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '探店的文字描述',
                           `liked` int(8) unsigned zerofill DEFAULT '00000000' COMMENT '點讚數量',
                           `comments` int(8) unsigned zerofill DEFAULT NULL COMMENT '評論數量',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;