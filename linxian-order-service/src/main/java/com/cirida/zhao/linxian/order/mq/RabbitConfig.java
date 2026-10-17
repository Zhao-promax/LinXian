package com.cirida.zhao.linxian.order.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑：
 * <ul>
 *   <li>延迟队列：order.delay.queue（TTL 到期转入死信 order.cancel.queue，触发超时取消）</li>
 *   <li>支付成功通知：order.paid.exchange（履约服务消费，触发分拣）</li>
 * </ul>
 */
@EnableRabbit
@Configuration
public class RabbitConfig {

    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_ROUTING_KEY = "order.delay";

    public static final String CANCEL_EXCHANGE = "order.cancel.exchange";
    public static final String CANCEL_QUEUE = "order.cancel.queue";
    public static final String CANCEL_ROUTING_KEY = "order.cancel";

    public static final String PAID_EXCHANGE = "order.paid.exchange";
    public static final String PAID_ROUTING_KEY = "order.paid";

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public Queue delayQueue(@Value("${order.timeout-millis}") long ttlMillis) {
        return QueueBuilder.durable(DELAY_QUEUE)
                .ttl((int) ttlMillis)
                .deadLetterExchange(CANCEL_EXCHANGE)
                .deadLetterRoutingKey(CANCEL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(DELAY_ROUTING_KEY);
    }

    @Bean
    public DirectExchange cancelExchange() {
        return new DirectExchange(CANCEL_EXCHANGE);
    }

    @Bean
    public Queue cancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE).build();
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(cancelQueue()).to(cancelExchange()).with(CANCEL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paidExchange() {
        return new DirectExchange(PAID_EXCHANGE);
    }
}
