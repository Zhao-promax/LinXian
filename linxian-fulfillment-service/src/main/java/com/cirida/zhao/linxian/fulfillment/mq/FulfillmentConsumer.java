package com.cirida.zhao.linxian.fulfillment.mq;

import com.cirida.zhao.linxian.fulfillment.service.FulfillmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消费订单支付成功消息，触发履约分拣。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentConsumer {

    private final FulfillmentService fulfillmentService;

    @RabbitListener(queues = FulfillmentRabbitConfig.PAID_QUEUE)
    public void onOrderPaid(String orderNo) {
        log.info("收到订单支付消息，开始履约分拣 orderNo={}", orderNo);
        fulfillmentService.startFulfillment(orderNo);
    }
}
