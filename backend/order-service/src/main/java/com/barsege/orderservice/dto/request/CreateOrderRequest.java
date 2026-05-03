package com.barsege.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        String userId,
        @NotEmpty @Valid List<CreateOrderItemRequest> items
) {
}
