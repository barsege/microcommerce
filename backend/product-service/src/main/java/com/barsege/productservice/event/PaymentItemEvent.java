package com.barsege.productservice.event;

public record PaymentItemEvent(
        Long productId,
        Integer quantity
) {
}