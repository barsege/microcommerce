package com.barsege.paymentservice.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        Long orderId,
        String userId,
        BigDecimal amount
) {
}