package com.barsege.orderservice.dto.response;

import com.barsege.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        OrderStatus status,
        List<OrderItemResponse> items
) {
}