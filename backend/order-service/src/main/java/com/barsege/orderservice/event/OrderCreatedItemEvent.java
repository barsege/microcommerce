package com.barsege.orderservice.event;

import java.math.BigDecimal;

public record OrderCreatedItemEvent(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) {
}