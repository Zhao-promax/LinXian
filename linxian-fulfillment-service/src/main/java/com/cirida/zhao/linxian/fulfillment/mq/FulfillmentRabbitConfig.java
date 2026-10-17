package com.cirida.zhao.linxian.fulfillment.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 履约服务 RabbitMQ 拓扑：声明并绑定"订单支付成功"队列。
 * <p>交换机 order.paid.exchange 与订单服务重复声明，声明是幂等的，保证任一服务先启动都能建好拓扑。</p>
 */
@EnableRabbit
@Configuration
public class FulfillmentRabbitConfig {

    public static final String PAID_EXCHANGE = "order.paid.exchange";
    public static final String PAID_QUEUE = "order.paid.queue";
    public static final String PAID_ROUTING_KEY = "order.paid";

    @Bean
    public DirectExchange paidExchange() {
        return new DirectExchange(PAID_EXCHANGE);
    }

    @Bean
    public Queue paidQueue() {
        return QueueBuilder.durable(PAID_QUEUE).build();
    }

    @Bean
    public Binding paidBinding() {
        return BindingBuilder.bind(paidQueue()).to(paidExchange()).with(PAID_ROUTING_KEY);
    }
}
