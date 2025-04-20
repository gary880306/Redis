package com.example.redis.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScrollResult {

    /** 資料列表 */
    private List<?> list;

    /** 本次回傳資料中的最小時間戳，用於下一次滑動載入的 max 參數 */
    private Long minTime;

    /** 若多筆資料具有相同時間戳，記錄已偏移的數量，避免重複載入 */
    private Integer offset;

    /** 是否還有更多資料可供載入 */
    private Boolean hasNext;
}
