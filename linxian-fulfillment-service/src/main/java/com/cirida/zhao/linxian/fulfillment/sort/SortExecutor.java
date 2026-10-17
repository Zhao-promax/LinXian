package com.cirida.zhao.linxian.fulfillment.sort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 并发分拣执行器：ThreadPoolExecutor + CountDownLatch。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>核心/最大线程数 8/16：分拣为 IO 密集（查库、打标），按 CPU 核数 ×2~4 估算；</li>
 *   <li>有界队列 200 + CallerRunsPolicy：队列打满时由调用线程兜底执行，宁可降速不丢任务；</li>
 *   <li>命名线程工厂：便于日志/线程转储定位分拣线程；</li>
 *   <li>CountDownLatch：主线程等待全部子任务完成后再汇总，替代笨重的 join。</li>
 * </ul>
 */
@Slf4j
@Component
public class SortExecutor {

    private final ThreadPoolExecutor executor;

    public SortExecutor(@Value("${sort.pool.core-size}") int coreSize,
                        @Value("${sort.pool.max-size}") int maxSize,
                        @Value("${sort.pool.queue-capacity}") int queueCapacity,
                        @Value("${sort.pool.keep-alive-seconds}") int keepAliveSeconds) {
        this.executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new CustomizableThreadFactory("linxian-sort-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 并发执行全部任务，等待所有任务完成后返回总耗时（毫秒）。
     */
    public long runConcurrently(List<Runnable> tasks) {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable task : tasks) {
            executor.execute(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("分拣等待被中断", e);
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * 串行执行全部任务，用于对比并发加速比。
     */
    public long runSerially(List<Runnable> tasks) {
        long start = System.currentTimeMillis();
        for (Runnable task : tasks) {
            task.run();
        }
        return System.currentTimeMillis() - start;
    }
}
