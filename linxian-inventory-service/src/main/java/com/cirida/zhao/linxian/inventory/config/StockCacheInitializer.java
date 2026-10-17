package com.cirida.zhao.linxian.inventory.config;

import com.cirida.zhao.linxian.inventory.entity.Stock;
import com.cirida.zhao.linxian.inventory.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把 DB 可用库存同步到 Redis，作为预扣的缓存副本。
 * <p>生产环境可改为独立的库存预热/对账任务。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockCacheInitializer implements ApplicationRunner {

    private static final String STOCK_KEY_PREFIX = "stock:";

    private final StockMapper stockMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<Stock> stocks = stockMapper.selectList(null);
        for (Stock stock : stocks) {
            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + stock.getSkuId(), String.valueOf(stock.getAvailable()));
        }
        log.info("库存缓存初始化完成，共 {} 个 SKU", stocks.size());
    }
}
