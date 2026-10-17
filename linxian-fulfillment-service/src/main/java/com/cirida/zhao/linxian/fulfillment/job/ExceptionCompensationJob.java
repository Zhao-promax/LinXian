package com.cirida.zhao.linxian.fulfillment.job;

import com.cirida.zhao.linxian.fulfillment.entity.FulfillmentException;
import com.cirida.zhao.linxian.fulfillment.service.FulfillmentExceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 履约异常定时补偿：扫描待处理且到达重试时间的异常，逐条重试并标记已补偿。
 * <p>与"订单超时取消延迟队列"形成两种补偿策略：延迟队列处理定时触发，本任务处理失败重试。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionCompensationJob {

    private final FulfillmentExceptionService exceptionService;

    @Scheduled(fixedDelayString = "${fulfillment.compensate-interval-ms:30000}")
    public void compensate() {
        List<FulfillmentException> pending = exceptionService.listPending();
        if (pending.isEmpty()) {
            return;
        }
        log.info("履约异常补偿：发现 {} 条待处理异常", pending.size());
        for (FulfillmentException exception : pending) {
            try {
                exceptionService.retry(exception);
            } catch (Exception e) {
                log.error("补偿异常失败 id={}", exception.getId(), e);
            }
        }
    }
}
