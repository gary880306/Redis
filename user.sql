/*
user_id: 使用 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT，作為自增的主鍵。
username: 用戶名稱，設計 VARCHAR(50) 即足夠。
email: 電子郵件地址，電商系統中通常需要唯一，故加上 UNIQUE KEY。
password: 存放經過雜湊（Hash）或加密後的密碼，建議長度至少保留 100～255。
phone: 聯絡電話，格式彈性可自訂。
address: 寄送地址；若有多組地址，需要另外設計關聯表或將此處改為文本欄位。
registration_date: 用戶註冊的時間，這裡使用 TIMESTAMP，並設定 NOT NULL DEFAULT CURRENT_TIMESTAMP，可在插入資料時自動寫入伺服器的目前時間。
*/

CREATE TABLE `user` (
                        `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用戶唯一識別碼',
                        `username` VARCHAR(50) NOT NULL COMMENT '用戶名稱',
                        `email` VARCHAR(100) NOT NULL COMMENT '電子郵件地址',
                        `password` VARCHAR(255) NOT NULL COMMENT '密碼（加密或雜湊後儲存）',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '聯絡電話',
                        `address` VARCHAR(255) DEFAULT NULL COMMENT '寄送地址',
                        `registration_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '註冊日期，自動寫入目前時間',
                        PRIMARY KEY (`user_id`),
                        UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- fakeData
INSERT INTO `user` (`username`, `email`, `password`, `phone`, `address`, `registration_date`)
VALUES ('阿賢', 'test@example.com', '12345678', '0987654321', '台北市中正區忠孝西路', NOW());
