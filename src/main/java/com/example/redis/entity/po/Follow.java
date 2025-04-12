package com.example.redis.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("follow")
public class Follow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主鍵
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用戶ID
     */
    private Long userId;

    /**
     * 關聯的用戶ID
     */
    private Long followUserId;

    /**
     * 創建時間
     */
    private LocalDateTime createTime;
}