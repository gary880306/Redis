package com.example.redis.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("blog")
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商店ID
     */
    private Long shopId;

    /**
     * 用戶ID
     */
    private Long userId;

    /**
     * 標題
     */
    private String title;

    /**
     * 探店的照片，最多9張，多張以","隔開
     */
    private String images;

    /**
     * 探店的文字描述
     */
    private String content;

    /**
     * 點讚數量
     */
    private Integer liked;

    /**
     * 評論數量
     */
    private Integer comments;

    /**
     * 創建時間
     */
    private Timestamp createTime;

    /**
     * 更新時間
     */
    private Timestamp updateTime;
}