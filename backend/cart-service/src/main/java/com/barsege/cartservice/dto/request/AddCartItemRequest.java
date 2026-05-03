package com.barsege.cartservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddCartItemRequest(
        String userId,
        @NotNull Long productId,
        @NotBlank String productName,
        @NotNull BigDecimal unitPrice,
        @NotNull @Min(1) Integer quantity
) {
}
