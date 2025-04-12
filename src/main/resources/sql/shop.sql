/*
shop_id: 使用 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT，作為自增的主鍵。
name: 商店名稱，設計 VARCHAR(100)，確保能存儲完整名稱。
owner: 商店擁有者，使用 VARCHAR(100) 存放店主姓名。
phone: 聯絡電話，設計為 VARCHAR(20)，方便存儲國際號碼格式。
address: 商店地址，使用 VARCHAR(255)，允許存儲較長的地址資訊。
created_at: 記錄商店創建時間，使用 TIMESTAMP 並設置 DEFAULT CURRENT_TIMESTAMP，在插入資料時自動填入時間。
*/


CREATE TABLE shop (
                      shop_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商店ID',
                      name VARCHAR(100) NOT NULL COMMENT '商店名稱',
                      owner VARCHAR(100) NOT NULL COMMENT '商店擁有者',
                      phone VARCHAR(20) NOT NULL COMMENT '聯絡電話',
                      address VARCHAR(255) NOT NULL COMMENT '商店地址',
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店資訊表';

-- add imageUrl
ALTER TABLE shop ADD COLUMN image_url VARCHAR(255) NOT NULL COMMENT '商店圖片URL';

-- fakeData
INSERT INTO shop (name, owner, phone, address, created_at)
VALUES ('星空咖啡館', '張三', '0912-345-678', '台北市信義區松高路123號', NOW());

