package com.barsege.productservice.event;

public record StockReservedEvent(
        Long orderId,
        String userId
) {
}