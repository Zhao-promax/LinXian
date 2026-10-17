package com.cirida.zhao.linxian.order.mq;

import com.cirida.zhao.linxian.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 延迟队列死信消费者：处理超时未支付订单的自动取消。
 * <p>handleTimeoutCancel 幂等（非待支付状态直接忽略），重复消费安全。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitConfig.CANCEL_QUEUE)
    public void onTimeoutCancel(String orderNo) {
        log.info("收到超时取消消息 orderNo={}", orderNo);
        orderService.handleTimeoutCancel(orderNo);
    }
}
