package com.barsege.productservice.event;

import java.math.BigDecimal;
import java.util.List;

public record PaymentCompletedEvent(
        Long orderId,
        String userId,
        BigDecimal amount,
        List<PaymentItemEvent> items
) {
}