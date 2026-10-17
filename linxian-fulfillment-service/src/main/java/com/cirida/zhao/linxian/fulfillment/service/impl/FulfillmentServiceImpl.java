package com.cirida.zhao.linxian.fulfillment.service.impl;

import com.cirida.zhao.linxian.api.order.OrderClient;
import com.cirida.zhao.linxian.api.order.dto.OrderFulfillmentDTO;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.fulfillment.entity.FulfillmentTask;
import com.cirida.zhao.linxian.fulfillment.entity.FulfillmentTaskItem;
import com.cirida.zhao.linxian.fulfillment.mapper.FulfillmentTaskItemMapper;
import com.cirida.zhao.linxian.fulfillment.mapper.FulfillmentTaskMapper;
import com.cirida.zhao.linxian.fulfillment.service.FulfillmentExceptionService;
import com.cirida.zhao.linxian.fulfillment.service.FulfillmentService;
import com.cirida.zhao.linxian.fulfillment.sort.SortExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 履约分拣主流程：订单支付 → 分拣任务聚合 → 线程池并发分拣 → 订单分拣完成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentServiceImpl implements FulfillmentService {

    private static final int TASK_PENDING = 0;
    private static final int TASK_SORTING = 1;
    private static final int TASK_DONE = 2;
    private static final int ITEM_PENDING = 0;
    private static final int ITEM_SORTED = 1;

    private final OrderClient orderClient;
    private final FulfillmentTaskMapper taskMapper;
    private final FulfillmentTaskItemMapper taskItemMapper;
    private final FulfillmentExceptionService exceptionService;
    private final SortExecutor sortExecutor;

    @Value("${sort.item-millis}")
    private long sortItemMillis;

    @Value("${sort.fail-rate}")
    private double failRate;

    @Override
    public void startFulfillment(String orderNo) {
        // 1. 订单进入分拣中（幂等，已分拣中则跳过）
        unwrap(orderClient.startSort(orderNo));

        // 2. 拉取订单履约明细（自提点/团长/商品）
        OrderFulfillmentDTO dto = unwrap(orderClient.getOrderFulfillment(orderNo));

        // 3. 创建分拣任务 + 明细
        FulfillmentTask task = new FulfillmentTask();
        task.setTaskNo(generateTaskNo());
        task.setPickupPointId(dto.getPickupPointId());
        task.setOrderCount(1);
        task.setStatus(TASK_PENDING);
        taskMapper.insert(task);

        List<FulfillmentTaskItem> items = new ArrayList<>();
        for (OrderFulfillmentDTO.Item it : dto.getItems()) {
            FulfillmentTaskItem item = new FulfillmentTaskItem();
            item.setTaskId(task.getId());
            item.setOrderId(dto.getOrderId());
            item.setOrderNo(orderNo);
            item.setSkuId(it.getSkuId());
            item.setQuantity(it.getQuantity());
            item.setStatus(ITEM_PENDING);
            taskItemMapper.insert(item);
            items.add(item);
        }

        task.setStatus(TASK_SORTING);
        taskMapper.updateById(task);

        // 4. 线程池并发分拣，主线程 CountDownLatch 等待
        List<Runnable> sortTasks = items.stream()
                .map(item -> (Runnable) () -> sortOne(item, dto.getLeaderId(), dto.getPickupPointId()))
                .toList();
        long elapsed = sortExecutor.runConcurrently(sortTasks);
        log.info("并发分拣完成 orderNo={} 商品数={} 耗时={}ms", orderNo, items.size(), elapsed);

        task.setStatus(TASK_DONE);
        taskMapper.updateById(task);

        // 5. 通知订单分拣完成：分拣中 → 待自提
        unwrap(orderClient.sortDone(orderNo));
    }

    private void sortOne(FulfillmentTaskItem item, Long leaderId, Long pickupPointId) {
        try {
            Thread.sleep(sortItemMillis);
            if (failRate > 0 && ThreadLocalRandom.current().nextDouble() < failRate) {
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
            item.setStatus(ITEM_SORTED);
            taskItemMapper.updateById(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("分拣失败 orderNo={} skuId={}", item.getOrderNo(), item.getSkuId(), e);
            exceptionService.record(item.getOrderId(), item.getOrderNo(), item.getSkuId(),
                    leaderId, pickupPointId, "SORT_FAILED", "分拣失败: " + e.getMessage());
        }
    }

    private String generateTaskNo() {
        return "TK" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private <T> T unwrap(Result<T> result) {
        if (result == null || !result.isSuccess()) {
            int code = result == null ? ErrorCode.SYSTEM_ERROR.getCode() : result.getCode();
            String message = result == null ? ErrorCode.SYSTEM_ERROR.getMessage() : result.getMessage();
            throw new BizException(code, message);
        }
        return result.getData();
    }
}
