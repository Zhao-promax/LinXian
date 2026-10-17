package com.cirida.zhao.linxian.api.inventory.dto;

import lombok.Data;

/**
 * 库存回补 / 确认请求，按 orderId 定位预扣流水。
 */
@Data
public class StockOperateRequest {

    private Long orderId;
    private String orderNo;
}
