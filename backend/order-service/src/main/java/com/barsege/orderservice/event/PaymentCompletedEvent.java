package com.barsege.orderservice.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        Long orderId,
        String userId,
        BigDecimal amount
) {
}