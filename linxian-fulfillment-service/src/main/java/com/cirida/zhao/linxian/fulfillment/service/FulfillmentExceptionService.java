package com.cirida.zhao.linxian.fulfillment.service;

import com.cirida.zhao.linxian.fulfillment.dto.FulfillmentExceptionDoc;
import com.cirida.zhao.linxian.fulfillment.entity.FulfillmentException;

import java.util.List;

public interface FulfillmentExceptionService {

    /** 记录履约异常：落库 + 同步 ES。 */
    void record(Long orderId, String orderNo, Long skuId, Long leaderId, Long pickupPointId,
                String type, String description);

    /** 多维检索（团长/商品/订单/时间区间），ES 不可用时降级为 DB 查询。 */
    List<FulfillmentExceptionDoc> search(Long leaderId, Long skuId, String orderNo, Long from, Long to);

    /** 查询待补偿异常（待处理且到达重试时间）。 */
    List<FulfillmentException> listPending();

    /** 补偿单条异常：重试次数 +1，标记已补偿，回写 DB + ES。 */
    void retry(FulfillmentException exception);
}
