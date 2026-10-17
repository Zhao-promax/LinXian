package com.cirida.zhao.linxian.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 下单请求体。
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "自提点不能为空")
    private Long pickupPointId;

    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "SKU 不能为空")
        private Long skuId;

        private String productName;

        private String skuSpec;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        private Integer quantity;

        @NotNull(message = "单价不能为空")
        private BigDecimal price;
    }
}
