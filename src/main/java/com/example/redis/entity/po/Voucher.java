package com.example.redis.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("voucher")
public class Voucher implements Serializable {

    /**
     * 優惠券ID (主鍵)
     */
    @TableId(value = "voucher_id", type = IdType.AUTO)
    private Long voucherId;

    /**
     * 商店ID
     */
    private Long shopId;

    /**
     * 優惠券標題
     */
    private String title;

    /**
     * 副標題
     */
    private String subTitle;

    /**
     * 使用規則
     */
    private String rules;

    /**
     * 支付金額
     */
    private Long payValue;

    /**
     * 折抵金額
     */
    private Long actualValue;

    /**
     * 優惠券類型，0:普通券，1:秒殺券
     */
    private Integer type;

    /**
     * 優惠券狀態，1:上架，2:下架，3:過期
     */
    private Integer status;

    /**
     * 創建時間
     */
    private LocalDateTime createTime;

    /**
     * 更新時間
     */
    private LocalDateTime updateTime;

}
