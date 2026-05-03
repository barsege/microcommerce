package com.barsege.paymentservice.dto;

public record PaymentResult(
        boolean success,
        String transactionId,
        String failureReason
) {
}
