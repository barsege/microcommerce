package com.barsege.productservice.event;

import java.math.BigDecimal;
import java.util.List;

public record StockReservedEvent(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        String currencyCode,
        List<StockItemEvent> items
) {
}