package com.cirida.zhao.linxian.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象。
 */
@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private Long pickupPointId;

    private Long leaderId;

    private BigDecimal totalAmount;

    private Integer status;

    private String statusDesc;

    private LocalDateTime paymentTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createdAt;

    private List<ItemVO> items;

    @Data
    public static class ItemVO {

        private Long skuId;

        private String productName;

        private String skuSpec;

        private Integer quantity;

        private BigDecimal price;
    }
}
