package com.barsege.paymentservice.event;

public record PaymentItemEvent(
        Long productId,
        Integer quantity
) {
}