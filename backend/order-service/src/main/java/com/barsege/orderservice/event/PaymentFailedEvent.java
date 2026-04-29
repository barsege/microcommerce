package com.barsege.orderservice.event;

public record PaymentFailedEvent(
        Long orderId,
        String userId,
        String reason
) {
}