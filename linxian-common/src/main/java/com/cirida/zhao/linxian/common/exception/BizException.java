package com.cirida.zhao.linxian.common.exception;

import com.cirida.zhao.linxian.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常。在业务层抛出，由 {@link GlobalExceptionHandler} 统一转换为 {@code Result}。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }
}
