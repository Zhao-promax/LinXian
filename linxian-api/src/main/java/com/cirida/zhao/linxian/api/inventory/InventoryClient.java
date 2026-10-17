package com.cirida.zhao.linxian.api.inventory;

import com.cirida.zhao.linxian.api.inventory.dto.PreDeductRequest;
import com.cirida.zhao.linxian.api.inventory.dto.StockOperateRequest;
import com.cirida.zhao.linxian.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务 Feign 客户端（供订单服务调用）。
 */
@FeignClient(name = "linxian-inventory-service")
public interface InventoryClient {

    /** 下单时原子预扣库存（幂等） */
    @PostMapping("/inventory/pre-deduct")
    Result<Void> preDeduct(@RequestBody PreDeductRequest request);

    /** 订单取消时回补库存（幂等） */
    @PostMapping("/inventory/rollback")
    Result<Void> rollback(@RequestBody StockOperateRequest request);

    /** 支付成功后确认占用库存（幂等） */
    @PostMapping("/inventory/confirm")
    Result<Void> confirm(@RequestBody StockOperateRequest request);
}
