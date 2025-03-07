package com.example.redis.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;   // 狀態碼
    private String message; // 訊息
    private T data;         // 泛型數據

    // **默認無參建構子（Lombok 也會自動生成）**
    public Result() {}

    // **提供完整參數建構子**
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // **僅有狀態碼和訊息的建構子**
    public Result(Integer code, String message) {
        this(code, message, null);
    }

    // **僅有狀態碼的建構子（預設訊息）**
    public Result(Integer code) {
        this(code, code == 200 ? "操作成功" : "操作失敗", null);
    }

    // **靜態方法，簡化 API 回應**
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}
