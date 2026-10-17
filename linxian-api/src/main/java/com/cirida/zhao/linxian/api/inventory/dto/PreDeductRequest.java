package com.cirida.zhao.linxian.api.inventory.dto;

import lombok.Data;

import java.util.List;

/**
 * 库存预扣请求。
 * <p>orderId 用于幂等唯一键兜底；items 为本次下单的 SKU 与数量。</p>
 */
@Data
public class PreDeductRequest {

    private Long orderId;
    private String orderNo;
    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer quantity;
    }
}
