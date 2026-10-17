package com.cirida.zhao.linxian.order.controller;

import com.cirida.zhao.linxian.api.order.dto.OrderFulfillmentDTO;
import com.cirida.zhao.linxian.common.constant.Constants;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.order.dto.CreateOrderRequest;
import com.cirida.zhao.linxian.order.dto.OrderVO;
import com.cirida.zhao.linxian.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口。用户接口经网关 JWT 透传 X-User-Id；internal 为履约服务内部回调。
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<OrderVO> create(@RequestHeader(value = Constants.HEADER_USER_ID, required = false) Long userId,
                                  @Valid @RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.createOrder(requireUser(userId), request));
    }

    @PostMapping("/pay/{orderNo}")
    public Result<Void> pay(@RequestHeader(value = Constants.HEADER_USER_ID, required = false) Long userId,
                            @PathVariable String orderNo) {
        orderService.pay(requireUser(userId), orderNo);
        return Result.ok();
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@RequestHeader(value = Constants.HEADER_USER_ID, required = false) Long userId,
                               @PathVariable String orderNo) {
        orderService.cancel(requireUser(userId), orderNo);
        return Result.ok();
    }

    @PostMapping("/pickup/{orderNo}")
    public Result<Void> pickup(@RequestHeader(value = Constants.HEADER_USER_ID, required = false) Long userId,
                               @PathVariable String orderNo) {
        orderService.pickup(requireUser(userId), orderNo);
        return Result.ok();
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@RequestHeader(value = Constants.HEADER_USER_ID, required = false) Long userId,
                                  @PathVariable String orderNo) {
        return Result.ok(orderService.getOrder(requireUser(userId), orderNo));
    }

    @GetMapping("/internal/{orderNo}/fulfillment")
    public Result<OrderFulfillmentDTO> getOrderFulfillment(@PathVariable String orderNo) {
        return Result.ok(orderService.getOrderFulfillment(orderNo));
    }

    @PostMapping("/internal/{orderNo}/start-sort")
    public Result<Void> startSort(@PathVariable String orderNo) {
        orderService.startSort(orderNo);
        return Result.ok();
    }

    @PostMapping("/internal/{orderNo}/sort-done")
    public Result<Void> sortDone(@PathVariable String orderNo) {
        orderService.sortDone(orderNo);
        return Result.ok();
    }

    private Long requireUser(Long userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
