package com.cirida.zhao.linxian.order.statemachine;

import com.cirida.zhao.linxian.common.enums.OrderEvent;
import com.cirida.zhao.linxian.common.enums.OrderStatus;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;

import java.util.EnumMap;
import java.util.Map;

/**
 * 手写订单状态机：状态 × 事件 → 目标状态，非法转移抛业务异常。
 * <p>相比引入 Spring StateMachine，转移表更直观、易讲清设计，也便于面试解释。</p>
 */
public final class OrderStateMachine {

    private static final Map<OrderStatus, Map<OrderEvent, OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        register(OrderStatus.PENDING_PAYMENT, OrderEvent.PAY, OrderStatus.PAID);
        register(OrderStatus.PENDING_PAYMENT, OrderEvent.TIMEOUT_CANCEL, OrderStatus.CANCELLED);
        register(OrderStatus.PENDING_PAYMENT, OrderEvent.CANCEL, OrderStatus.CANCELLED);
        register(OrderStatus.PAID, OrderEvent.SORT, OrderStatus.SORTING);
        register(OrderStatus.SORTING, OrderEvent.SORT_DONE, OrderStatus.READY_FOR_PICKUP);
        register(OrderStatus.READY_FOR_PICKUP, OrderEvent.PICKUP, OrderStatus.COMPLETED);
    }

    private OrderStateMachine() {
    }

    public static OrderStatus transition(OrderStatus current, OrderEvent event) {
        Map<OrderEvent, OrderStatus> eventMap = TRANSITIONS.get(current);
        OrderStatus target = eventMap == null ? null : eventMap.get(event);
        if (target == null) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }
        return target;
    }

    private static void register(OrderStatus from, OrderEvent event, OrderStatus to) {
        TRANSITIONS.computeIfAbsent(from, k -> new EnumMap<>(OrderEvent.class)).put(event, to);
    }
}
