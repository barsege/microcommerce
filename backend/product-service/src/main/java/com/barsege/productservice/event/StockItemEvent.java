package com.barsege.productservice.event;

public record StockItemEvent(
        Long productId,
        Integer quantity
) {
}