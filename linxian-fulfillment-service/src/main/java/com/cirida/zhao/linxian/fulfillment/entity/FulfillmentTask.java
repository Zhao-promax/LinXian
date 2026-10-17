package com.cirida.zhao.linxian.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分拣任务（按自提点聚合）。
 * <p>status：0 待分拣，1 分拣中，2 已完成</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fulfillment_task")
public class FulfillmentTask extends BaseEntity {

    private String taskNo;

    private Long pickupPointId;

    private Integer orderCount;

    private Integer status;
}
