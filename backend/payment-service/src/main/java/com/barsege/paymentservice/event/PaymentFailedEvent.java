package com.barsege.paymentservice.event;

import java.util.List;

public record PaymentFailedEvent(
        Long orderId,
        String userId,
        String reason,
        List<PaymentItemEvent> items
) {
}