package com.barsege.paymentservice.event;

import java.math.BigDecimal;
import java.util.List;

public record StockReservedEvent(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        String currencyCode,
        List<PaymentItemEvent> items
) {
}