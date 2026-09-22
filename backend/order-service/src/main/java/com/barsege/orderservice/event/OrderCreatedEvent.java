package com.barsege.orderservice.event;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        String userId,
        String currencyCode,
        BigDecimal totalAmount,
        List<OrderCreatedItemEvent> items
) {
}