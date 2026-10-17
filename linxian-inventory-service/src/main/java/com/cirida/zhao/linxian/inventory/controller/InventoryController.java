package com.cirida.zhao.linxian.inventory.controller;

import com.cirida.zhao.linxian.api.inventory.dto.PreDeductRequest;
import com.cirida.zhao.linxian.api.inventory.dto.StockOperateRequest;
import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存接口，路径与 InventoryClient Feign 契约保持一致。
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final StockService stockService;

    @PostMapping("/pre-deduct")
    public Result<Void> preDeduct(@RequestBody PreDeductRequest request) {
        stockService.preDeduct(request);
        return Result.ok();
    }

    @PostMapping("/rollback")
    public Result<Void> rollback(@RequestBody StockOperateRequest request) {
        stockService.rollback(request);
        return Result.ok();
    }

    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestBody StockOperateRequest request) {
        stockService.confirm(request);
        return Result.ok();
    }
}
