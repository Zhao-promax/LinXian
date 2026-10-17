package com.cirida.zhao.linxian.api.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存回补 / 确认请求，按 orderId 定位预扣流水。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockOperateRequest {

    private Long orderId;
    private String orderNo;
}
