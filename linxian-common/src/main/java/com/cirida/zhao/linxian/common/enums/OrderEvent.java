package com.cirida.zhao.linxian.common.enums;

/**
 * 订单状态机事件。
 */
public enum OrderEvent {

    /** 创建订单（进入待支付） */
    CREATE,

    /** 支付成功（待支付 → 已支付） */
    PAY,

    /** 超时未支付（待支付 → 已取消） */
    TIMEOUT_CANCEL,

    /** 开始分拣（已支付 → 分拣中） */
    SORT,

    /** 分拣完成（分拣中 → 待自提） */
    SORT_DONE,

    /** 用户自提（待自提 → 已自提） */
    PICKUP,

    /** 主动取消（待支付 → 已取消） */
    CANCEL
}
