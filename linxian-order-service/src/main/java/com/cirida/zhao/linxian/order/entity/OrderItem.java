package com.cirida.zhao.linxian.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细（冗余商品名/规格/单价，下单时快照）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;

    private Long skuId;

    private String productName;

    private String skuSpec;

    private Integer quantity;

    private BigDecimal price;
}
