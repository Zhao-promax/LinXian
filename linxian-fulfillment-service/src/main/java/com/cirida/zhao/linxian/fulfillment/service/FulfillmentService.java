package com.cirida.zhao.linxian.fulfillment.service;

public interface FulfillmentService {

    /**
     * 收到订单支付消息后执行履约分拣：回调订单状态 → 落分拣任务 → 并发分拣 → 通知订单分拣完成。
     */
    void startFulfillment(String orderNo);
}
