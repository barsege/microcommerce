package com.barsege.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        Long orderId,
        String userId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String cardHolderName,
        String cardNumber,
        String expireMonth,
        String expireYear,
        String cvc
) {
}
