package com.barsege.cartservice.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        String userId,
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
}