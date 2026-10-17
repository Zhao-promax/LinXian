package com.cirida.zhao.linxian.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cirida.zhao.linxian.api.inventory.InventoryClient;
import com.cirida.zhao.linxian.api.inventory.dto.PreDeductRequest;
import com.cirida.zhao.linxian.api.inventory.dto.StockOperateRequest;
import com.cirida.zhao.linxian.api.leader.LeaderClient;
import com.cirida.zhao.linxian.api.leader.dto.PickupPointDTO;
import com.cirida.zhao.linxian.api.order.dto.OrderFulfillmentDTO;
import com.cirida.zhao.linxian.common.enums.OrderEvent;
import com.cirida.zhao.linxian.common.enums.OrderStatus;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.order.dto.CreateOrderRequest;
import com.cirida.zhao.linxian.order.dto.OrderVO;
import com.cirida.zhao.linxian.order.entity.Order;
import com.cirida.zhao.linxian.order.entity.OrderItem;
import com.cirida.zhao.linxian.order.mapper.OrderItemMapper;
import com.cirida.zhao.linxian.order.mapper.OrderMapper;
import com.cirida.zhao.linxian.order.mq.RabbitConfig;
import com.cirida.zhao.linxian.order.service.OrderService;
import com.cirida.zhao.linxian.order.statemachine.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单核心：下单（Feign 预扣 + 校验自提点）、状态机流转、mock 支付、延迟队列超时取消。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final LeaderClient leaderClient;
    private final InventoryClient inventoryClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, CreateOrderRequest request) {
        // 1. 校验自提点，拿到团长 ID
        PickupPointDTO point = unwrap(leaderClient.getPickupPoint(request.getPickupPointId()));

        // 2. 落库订单 + 明细（本地事务）
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPickupPointId(request.getPickupPointId());
        order.setLeaderId(point.getLeaderId());
        order.setTotalAmount(calcTotal(request.getItems()));
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        orderMapper.insert(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.Item it : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(it.getSkuId());
            item.setProductName(it.getProductName());
            item.setSkuSpec(it.getSkuSpec());
            item.setQuantity(it.getQuantity());
            item.setPrice(it.getPrice());
            orderItemMapper.insert(item);
            orderItems.add(item);
        }

        // 3. 调库存预扣（Feign，幂等）
        PreDeductRequest pre = new PreDeductRequest();
        pre.setOrderId(order.getId());
        pre.setOrderNo(order.getOrderNo());
        pre.setItems(request.getItems().stream().map(i -> {
            PreDeductRequest.Item p = new PreDeductRequest.Item();
            p.setSkuId(i.getSkuId());
            p.setQuantity(i.getQuantity());
            return p;
        }).toList());
        unwrap(inventoryClient.preDeduct(pre));

        // 4. 发送延迟队列消息，超时未支付自动取消
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.DELAY_EXCHANGE, RabbitConfig.DELAY_ROUTING_KEY, order.getOrderNo());
        } catch (Exception e) {
            log.error("下单成功但发送延迟消息失败，回补库存 orderNo={}", order.getOrderNo(), e);
            inventoryClient.rollback(new StockOperateRequest(order.getId(), order.getOrderNo()));
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }

        return toVO(order, orderItems);
    }

    @Override
    public void pay(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        checkOwner(order, userId);

        if (fireEvent(order, OrderEvent.PAY, LocalDateTime.now(), null) == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 确认占用库存（幂等）
        unwrap(inventoryClient.confirm(new StockOperateRequest(order.getId(), orderNo)));

        // 异步通知履约服务开始分拣
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.PAID_EXCHANGE, RabbitConfig.PAID_ROUTING_KEY, orderNo);
        } catch (Exception e) {
            log.error("支付成功但通知履约失败，等待补偿 orderNo={}", orderNo, e);
        }
    }

    @Override
    public void cancel(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        checkOwner(order, userId);

        if (fireEvent(order, OrderEvent.CANCEL, null, LocalDateTime.now()) == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }
        unwrap(inventoryClient.rollback(new StockOperateRequest(order.getId(), orderNo)));
    }

    @Override
    public void pickup(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        checkOwner(order, userId);

        if (fireEvent(order, OrderEvent.PICKUP, null, null) == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }
    }

    @Override
    public OrderVO getOrder(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        checkOwner(order, userId);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        return toVO(order, items);
    }

    @Override
    public OrderFulfillmentDTO getOrderFulfillment(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        OrderFulfillmentDTO dto = new OrderFulfillmentDTO();
        dto.setOrderId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setPickupPointId(order.getPickupPointId());
        dto.setLeaderId(order.getLeaderId());
        dto.setItems(items.stream().map(i -> {
            OrderFulfillmentDTO.Item item = new OrderFulfillmentDTO.Item();
            item.setSkuId(i.getSkuId());
            item.setProductName(i.getProductName());
            item.setQuantity(i.getQuantity());
            return item;
        }).toList());
        return dto;
    }

    @Override
    public void startSort(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == OrderStatus.PAID.getCode()) {
            fireEvent(order, OrderEvent.SORT, null, null);
        }
    }

    @Override
    public void sortDone(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == OrderStatus.SORTING.getCode()) {
            fireEvent(order, OrderEvent.SORT_DONE, null, null);
        }
    }

    @Override
    public void handleTimeoutCancel(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT.getCode()) {
            if (fireEvent(order, OrderEvent.TIMEOUT_CANCEL, null, LocalDateTime.now()) > 0) {
                unwrap(inventoryClient.rollback(new StockOperateRequest(order.getId(), orderNo)));
            }
        }
    }

    private int fireEvent(Order order, OrderEvent event, LocalDateTime paymentTime, LocalDateTime cancelTime) {
        OrderStatus current = OrderStatus.of(order.getStatus());
        OrderStatus target = OrderStateMachine.transition(current, event);
        return orderMapper.casStatus(order.getId(), current.getCode(), target.getCode(), paymentTime, cancelTime);
    }

    private Order getByOrderNo(String orderNo) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
    }

    private void checkOwner(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private <T> T unwrap(Result<T> result) {
        if (result == null || !result.isSuccess()) {
            int code = result == null ? ErrorCode.SYSTEM_ERROR.getCode() : result.getCode();
            String message = result == null ? ErrorCode.SYSTEM_ERROR.getMessage() : result.getMessage();
            throw new BizException(code, message);
        }
        return result.getData();
    }

    private BigDecimal calcTotal(List<CreateOrderRequest.Item> items) {
        return items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNo() {
        return "LX" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private OrderVO toVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setPickupPointId(order.getPickupPointId());
        vo.setLeaderId(order.getLeaderId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(OrderStatus.of(order.getStatus()).getDesc());
        vo.setPaymentTime(order.getPaymentTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(items.stream().map(i -> {
            OrderVO.ItemVO iv = new OrderVO.ItemVO();
            iv.setSkuId(i.getSkuId());
            iv.setProductName(i.getProductName());
            iv.setSkuSpec(i.getSkuSpec());
            iv.setQuantity(i.getQuantity());
            iv.setPrice(i.getPrice());
            return iv;
        }).toList());
        return vo;
    }
}
