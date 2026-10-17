package com.cirida.zhao.linxian.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 履约异常（落库后同步 Elasticsearch 供多维检索）。
 * <p>status：0 待处理，1 已补偿</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fulfillment_exception")
public class FulfillmentException extends BaseEntity {

    private Long orderId;

    private String orderNo;

    private Long skuId;

    private Long leaderId;

    private Long pickupPointId;

    private String exceptionType;

    private String description;

    private Integer status;

    private Integer retryCount;

    private LocalDateTime nextRetryTime;
}
