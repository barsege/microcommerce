package com.barsege.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotNull Long productId,
        @NotBlank String productName,
        @NotNull BigDecimal unitPrice,
        @NotNull @Min(1) Integer quantity
) {
}