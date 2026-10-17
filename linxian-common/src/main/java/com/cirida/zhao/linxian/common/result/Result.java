package com.cirida.zhao.linxian.common.result;

import lombok.Data;

/**
 * 统一响应体。
 * <p>code = 0 表示成功，非 0 表示业务/系统错误，message 为提示信息。</p>
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return build(ErrorCode.SUCCESS, null);
    }

    public static <T> Result<T> ok(T data) {
        return build(ErrorCode.SUCCESS, data);
    }

    public static <T> Result<T> fail(String message) {
        return fail(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    public boolean isSuccess() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }

    private static <T> Result<T> build(ErrorCode errorCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setData(data);
        return result;
    }
}
