package com.cirida.zhao.linxian.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 履约异常 Elasticsearch 文档。
 * <p>时间字段用 epoch 毫秒（long）存储，便于 range 区间检索，也避免本地日期类型序列化问题。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentExceptionDoc {

    private Long id;

    private Long orderId;

    private String orderNo;

    private Long skuId;

    private Long leaderId;

    private Long pickupPointId;

    private String exceptionType;

    private String description;

    private Integer status;

    private Integer retryCount;

    /** 创建时间（epoch 毫秒） */
    private Long createdAt;
}
