package com.example.redis.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("seckill_voucher")
public class SeckillVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 關聯的優惠券ID
     */
    @TableId(value = "voucher_id")
    private Long voucherId;

    /**
     * 庫存
     */
    private Integer stock;

    /**
     * 創建時間
     */
    private LocalDateTime createTime;

    /**
     * 生效時間
     */
    private LocalDateTime beginTime;

    /**
     * 失效時間
     */
    private LocalDateTime endTime;

    /**
     * 更新時間
     */
    private LocalDateTime updateTime;
}