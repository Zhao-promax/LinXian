package com.cirida.zhao.linxian.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存预扣流水表。唯一索引 (order_id, sku_id, type) 兜底防重复扣减。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_deduct_log")
public class StockDeductLog extends BaseEntity {

    private Long orderId;

    private String orderNo;

    private Long skuId;

    private Integer quantity;

    /** 1 预扣 2 回补 3 确认 */
    private Integer type;
}
