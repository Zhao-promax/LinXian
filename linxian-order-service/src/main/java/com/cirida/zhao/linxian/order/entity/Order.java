package com.cirida.zhao.linxian.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单。状态取值见 {@link com.cirida.zhao.linxian.common.enums.OrderStatus}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private String orderNo;

    private Long userId;

    private Long pickupPointId;

    private Long leaderId;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime paymentTime;

    private LocalDateTime cancelTime;
}
