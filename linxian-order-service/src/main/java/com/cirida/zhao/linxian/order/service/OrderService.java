package com.cirida.zhao.linxian.order.service;

import com.cirida.zhao.linxian.api.order.dto.OrderFulfillmentDTO;
import com.cirida.zhao.linxian.order.dto.CreateOrderRequest;
import com.cirida.zhao.linxian.order.dto.OrderVO;

public interface OrderService {

    OrderVO createOrder(Long userId, CreateOrderRequest request);

    void pay(Long userId, String orderNo);

    void cancel(Long userId, String orderNo);

    void pickup(Long userId, String orderNo);

    OrderVO getOrder(Long userId, String orderNo);

    OrderFulfillmentDTO getOrderFulfillment(String orderNo);

    void startSort(String orderNo);

    void sortDone(String orderNo);

    void handleTimeoutCancel(String orderNo);
}
