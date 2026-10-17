package com.cirida.zhao.linxian.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态机状态定义。
 * <p>正向链路：待支付 → 已支付 → 分拣中 → 待自提 → 已自提；异常分支：待支付 → 已取消。</p>
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付/待履约"),
    SORTING(2, "分拣中"),
    READY_FOR_PICKUP(3, "待自提"),
    COMPLETED(4, "已自提"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String desc;

    public static OrderStatus of(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}
