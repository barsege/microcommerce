package com.barsege.cartservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemQuantityRequest(
        @NotNull @Min(1) Integer quantity
) {
}
