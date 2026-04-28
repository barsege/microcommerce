package com.barsege.productservice.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Integer reservedStock,
        Integer availableStock,
        String category,
        String imageUrl
) {
}
