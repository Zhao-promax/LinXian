package com.cirida.zhao.linxian.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码。
 * <p>0 成功；4xx 参数/鉴权类；5xx 系统类；1xxx 起为业务错误码。</p>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    // 业务错误码
    USER_EXISTS(1001, "用户已存在"),
    LOGIN_FAILED(1002, "用户名或密码错误"),
    STOCK_NOT_ENOUGH(2001, "库存不足"),
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态不允许该操作"),
    PICKUP_POINT_NOT_FOUND(4001, "自提点不存在");

    private final int code;
    private final String message;
}
