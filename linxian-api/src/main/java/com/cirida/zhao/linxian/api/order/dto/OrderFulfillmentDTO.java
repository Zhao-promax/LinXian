package com.cirida.zhao.linxian.api.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单履约信息（供履约服务分拣聚合使用）。
 */
@Data
public class OrderFulfillmentDTO {

    private Long orderId;

    private String orderNo;

    private Long pickupPointId;

    private Long leaderId;

    private List<Item> items;

    @Data
    public static class Item {

        private Long skuId;

        private String productName;

        private Integer quantity;
    }
}
