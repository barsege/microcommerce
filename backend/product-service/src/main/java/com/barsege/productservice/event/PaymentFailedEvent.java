package com.barsege.productservice.event;

import java.util.List;

public record PaymentFailedEvent(
        Long orderId,
        String userId,
        String reason,
        List<StockItemEvent> items
) {
}