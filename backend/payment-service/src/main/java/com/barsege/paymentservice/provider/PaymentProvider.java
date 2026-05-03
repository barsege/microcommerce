package com.barsege.paymentservice.provider;

import com.barsege.paymentservice.dto.PaymentRequest;
import com.barsege.paymentservice.dto.PaymentResult;

public interface PaymentProvider {

    PaymentResult pay(PaymentRequest request);

    com.barsege.paymentservice.entity.PaymentProvider providerType();
}
