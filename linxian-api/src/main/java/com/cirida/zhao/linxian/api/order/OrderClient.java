package com.cirida.zhao.linxian.api.order;

import com.cirida.zhao.linxian.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 订单服务内部 Feign 客户端（供履约服务回调订单状态）。
 */
@FeignClient(name = "linxian-order-service")
public interface OrderClient {

    /** 开始分拣：已支付 → 分拣中 */
    @PostMapping("/order/internal/{orderNo}/start-sort")
    Result<Void> startSort(@PathVariable("orderNo") String orderNo);

    /** 分拣完成：分拣中 → 待自提 */
    @PostMapping("/order/internal/{orderNo}/sort-done")
    Result<Void> sortDone(@PathVariable("orderNo") String orderNo);
}
