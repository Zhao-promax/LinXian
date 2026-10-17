package com.cirida.zhao.linxian.fulfillment.controller;

import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.fulfillment.dto.FulfillmentExceptionDoc;
import com.cirida.zhao.linxian.fulfillment.service.FulfillmentExceptionService;
import com.cirida.zhao.linxian.fulfillment.sort.SortExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 履约接口：分拣并发基准、履约异常多维检索（团长/商品/订单/时间区间）。
 */
@RestController
@RequestMapping("/fulfillment")
@RequiredArgsConstructor
public class FulfillmentController {

    private final SortExecutor sortExecutor;
    private final FulfillmentExceptionService exceptionService;

    /**
     * 串行 vs 并发分拣基准：默认 4 件 × 800ms，串行约 3.2s，并发约 0.8s。
     */
    @PostMapping("/benchmark")
    public Result<Map<String, Object>> benchmark(@RequestParam(defaultValue = "4") int itemCount,
                                                 @RequestParam(defaultValue = "800") long sleepMillis) {
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            tasks.add(() -> sleepQuietly(sleepMillis));
        }
        long serialMs = sortExecutor.runSerially(tasks);
        long concurrentMs = sortExecutor.runConcurrently(tasks);

        Map<String, Object> result = new HashMap<>();
        result.put("itemCount", itemCount);
        result.put("perItemMillis", sleepMillis);
        result.put("serialMs", serialMs);
        result.put("concurrentMs", concurrentMs);
        result.put("speedup", String.format("%.2fx", (double) serialMs / Math.max(1, concurrentMs)));
        return Result.ok(result);
    }

    /**
     * 履约异常多维组合检索。time 参数为 epoch 毫秒。
     */
    @GetMapping("/exception/search")
    public Result<List<FulfillmentExceptionDoc>> search(@RequestParam(required = false) Long leaderId,
                                                        @RequestParam(required = false) Long skuId,
                                                        @RequestParam(required = false) String orderNo,
                                                        @RequestParam(required = false) Long from,
                                                        @RequestParam(required = false) Long to) {
        return Result.ok(exceptionService.search(leaderId, skuId, orderNo, from, to));
    }

    /**
     * 演示用：造一条履约异常，便于观察定时补偿与 ES 检索。
     */
    @PostMapping("/exception/simulate")
    public Result<Void> simulate(@RequestParam(required = false) Long leaderId,
                                 @RequestParam(required = false) Long skuId) {
        exceptionService.record(null, "SIM" + System.currentTimeMillis(),
                skuId, leaderId, null, "SIMULATED", "演示用履约异常");
        return Result.ok();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
