package com.example.redis.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("shop")
public class Shop implements Serializable {

    @TableId(value = "shop_id")
    private Long shopId;         // 商店ID
    private String name;         // 商店名稱
    private String owner;        // 商店擁有者
    private String phone;        // 聯絡電話
    private String address;      // 商店地址
    private String imageUrl;      // 圖片URL
    private Timestamp createdAt; // 創建時間
}
