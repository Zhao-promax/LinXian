package com.cirida.zhao.linxian.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分拣任务明细（一件商品一条）。
 * <p>status：0 待分拣，1 已分拣</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fulfillment_task_item")
public class FulfillmentTaskItem extends BaseEntity {

    private Long taskId;

    private Long orderId;

    private String orderNo;

    private Long skuId;

    private Integer quantity;

    private Integer status;
}
