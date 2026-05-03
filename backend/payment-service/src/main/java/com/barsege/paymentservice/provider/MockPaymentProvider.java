package com.barsege.paymentservice.provider;

import com.barsege.paymentservice.dto.PaymentRequest;
import com.barsege.paymentservice.dto.PaymentResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockPaymentProvider implements PaymentProvider {

    private final boolean forceFailure;

    public MockPaymentProvider(@Value("${payment.mock.force-failure:false}") boolean forceFailure) {
        this.forceFailure = forceFailure;
    }

    @PostConstruct
    void logProviderSelected() {
        log.info("Payment provider selected: mock");
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        if (forceFailure) {
            return new PaymentResult(false, null, "Fake payment failed for testing");
        }

        return new PaymentResult(true, "mock-" + UUID.randomUUID(), null);
    }

    @Override
    public com.barsege.paymentservice.entity.PaymentProvider providerType() {
        return com.barsege.paymentservice.entity.PaymentProvider.FAKE;
    }
}
