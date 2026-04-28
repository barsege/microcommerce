package com.barsege.productservice.event;

public record StockReservationFailedEvent(
        Long orderId,
        String userId,
        String reason
) {
}