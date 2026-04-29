package com.barsege.paymentservice.event;

import java.util.List;

public record StockReservedEvent(
        Long orderId,
        String userId,
        List<PaymentItemEvent> items
) {
}