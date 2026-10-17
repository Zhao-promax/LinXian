package com.cirida.zhao.linxian.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cirida.zhao.linxian.api.inventory.dto.PreDeductRequest;
import com.cirida.zhao.linxian.api.inventory.dto.StockOperateRequest;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.inventory.entity.StockDeductLog;
import com.cirida.zhao.linxian.inventory.enums.StockDeductType;
import com.cirida.zhao.linxian.inventory.mapper.StockDeductLogMapper;
import com.cirida.zhao.linxian.inventory.mapper.StockMapper;
import com.cirida.zhao.linxian.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存核心：Redis+Lua 原子预扣（防超卖）+ DB 唯一索引兜底（幂等）+ DB 条件更新（双保险）。
 * <p>Redis 与 DB 双写，Redis 扣减后 DB 失败会回补 Redis；极端漂移由履约服务的定时补偿对账。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private static final String STOCK_KEY_PREFIX = "stock:";

    private final StringRedisTemplate redisTemplate;
    private final StockMapper stockMapper;
    private final StockDeductLogMapper deductLogMapper;
    private final DefaultRedisScript<Long> preDeductScript;

    @Override
    public void preDeduct(PreDeductRequest request) {
        if (request.getOrderId() == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR);
        }
        for (PreDeductRequest.Item item : request.getItems()) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BizException(ErrorCode.PARAM_ERROR);
            }
        }

        Long orderId = request.getOrderId();
        // 幂等：同一订单已预扣过，直接返回
        if (countLog(orderId, null, StockDeductType.PRE_DEDUCT) > 0) {
            return;
        }

        List<PreDeductRequest.Item> items = request.getItems();
        List<String> keys = items.stream().map(i -> stockKey(i.getSkuId())).toList();
        Object[] args = items.stream().map(i -> String.valueOf(i.getQuantity())).toArray();

        // 1. Redis + Lua 原子预扣（防超卖主路径）
        Long result = redisTemplate.execute(preDeductScript, keys, args);
        if (result == null || result == -1) {
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "库存未初始化，请先同步库存到 Redis");
        }
        if (result == 0) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        // 2. DB 唯一索引兜底 + 条件更新双保险
        for (PreDeductRequest.Item item : items) {
            boolean first = tryInsertLog(orderId, request.getOrderNo(), item.getSkuId(), item.getQuantity(), StockDeductType.PRE_DEDUCT);
            if (!first) {
                // 并发下唯一索引兜底：该 SKU 已被处理，回补本次 Redis 已扣部分
                restoreRedis(item.getSkuId(), item.getQuantity());
                continue;
            }
            if (stockMapper.deductAvailable(item.getSkuId(), item.getQuantity()) == 0) {
                restoreRedis(item.getSkuId(), item.getQuantity());
                throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
            }
        }
    }

    @Override
    public void rollback(StockOperateRequest request) {
        List<StockDeductLog> preLogs = listLogs(request.getOrderId(), null, StockDeductType.PRE_DEDUCT);
        if (preLogs.isEmpty()) {
            return;
        }
        for (StockDeductLog pre : preLogs) {
            boolean first = tryInsertLog(pre.getOrderId(), pre.getOrderNo(), pre.getSkuId(), pre.getQuantity(), StockDeductType.ROLLBACK);
            if (!first) {
                continue;
            }
            restoreRedis(pre.getSkuId(), pre.getQuantity());
            stockMapper.restoreAvailable(pre.getSkuId(), pre.getQuantity());
        }
    }

    @Override
    public void confirm(StockOperateRequest request) {
        List<StockDeductLog> preLogs = listLogs(request.getOrderId(), null, StockDeductType.PRE_DEDUCT);
        if (preLogs.isEmpty()) {
            return;
        }
        for (StockDeductLog pre : preLogs) {
            boolean first = tryInsertLog(pre.getOrderId(), pre.getOrderNo(), pre.getSkuId(), pre.getQuantity(), StockDeductType.CONFIRM);
            if (!first) {
                continue;
            }
            if (stockMapper.confirmLocked(pre.getSkuId(), pre.getQuantity()) == 0) {
                throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "确认占用失败：锁定库存不足");
            }
        }
    }

    private boolean tryInsertLog(Long orderId, String orderNo, Long skuId, Integer quantity, StockDeductType type) {
        StockDeductLog log = new StockDeductLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setSkuId(skuId);
        log.setQuantity(quantity);
        log.setType(type.getCode());
        try {
            deductLogMapper.insert(log);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    private long countLog(Long orderId, Long skuId, StockDeductType type) {
        Long count = deductLogMapper.selectCount(new LambdaQueryWrapper<StockDeductLog>()
                .eq(StockDeductLog::getOrderId, orderId)
                .eq(skuId != null, StockDeductLog::getSkuId, skuId)
                .eq(StockDeductLog::getType, type.getCode()));
        return count == null ? 0 : count;
    }

    private List<StockDeductLog> listLogs(Long orderId, Long skuId, StockDeductType type) {
        return deductLogMapper.selectList(new LambdaQueryWrapper<StockDeductLog>()
                .eq(StockDeductLog::getOrderId, orderId)
                .eq(skuId != null, StockDeductLog::getSkuId, skuId)
                .eq(StockDeductLog::getType, type.getCode()));
    }

    private void restoreRedis(Long skuId, Integer quantity) {
        redisTemplate.opsForValue().increment(stockKey(skuId), quantity);
    }

    private String stockKey(Long skuId) {
        return STOCK_KEY_PREFIX + skuId;
    }
}
