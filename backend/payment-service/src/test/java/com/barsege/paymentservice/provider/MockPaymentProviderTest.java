package com.barsege.paymentservice.provider;

import com.barsege.paymentservice.dto.PaymentRequest;
import com.barsege.paymentservice.dto.PaymentResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentProviderTest {

    @Test
    void shouldReturnSuccessWhenForceFailureIsFalse() {
        MockPaymentProvider provider = new MockPaymentProvider(false);

        PaymentResult result = provider.pay(request());

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).startsWith("mock-");
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void shouldReturnFailureWhenForceFailureIsTrue() {
        MockPaymentProvider provider = new MockPaymentProvider(true);

        PaymentResult result = provider.pay(request());

        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).isNull();
        assertThat(result.failureReason()).isEqualTo("Fake payment failed for testing");
    }

    private PaymentRequest request() {
        return new PaymentRequest(
                1L,
                "user-1",
                BigDecimal.ONE,
                "TRY",
                "MOCK",
                null,
                null,
                null,
                null,
                null
        );
    }
}
