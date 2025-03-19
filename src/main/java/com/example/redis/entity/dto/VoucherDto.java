package com.example.redis.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherDto {
    /**
     * 優惠券ID
     */
    private Long id;

    /**
     * 商店ID
     */
    private Long shopId;

    /**
     * 標題
     */
    private String title;

    /**
     * 副標題
     */
    private String subTitle;

    /**
     * 規則
     */
    private String rules;

    /**
     * 支付金額
     */
    private Long payValue;

    /**
     * 實際抵扣金額
     */
    private Long actualValue;

    /**
     * 類型
     */
    private Integer type;

    /**
     * 庫存
     */
    private Integer stock;

    /**
     * 生效時間
     */
    private LocalDateTime beginTime;

    /**
     * 失效時間
     */
    private LocalDateTime endTime;
}