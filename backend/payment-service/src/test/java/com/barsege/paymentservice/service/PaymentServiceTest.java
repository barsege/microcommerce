package com.barsege.paymentservice.service;

import com.barsege.paymentservice.dto.PaymentResult;
import com.barsege.paymentservice.entity.Payment;
import com.barsege.paymentservice.event.PaymentCompletedEvent;
import com.barsege.paymentservice.event.PaymentFailedEvent;
import com.barsege.paymentservice.event.StockReservedEvent;
import com.barsege.paymentservice.messaging.PaymentEventPublisher;
import com.barsege.paymentservice.provider.PaymentProvider;
import com.barsege.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private PaymentEventPublisher paymentEventPublisher;
    private PaymentProvider paymentProvider;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentEventPublisher = mock(PaymentEventPublisher.class);
        paymentProvider = mock(PaymentProvider.class);
        paymentService = new PaymentService(paymentRepository, paymentEventPublisher, paymentProvider);
        ReflectionTestUtils.setField(paymentService, "currency", "TRY");
        ReflectionTestUtils.setField(paymentService, "paymentMethod", "MOCK");
        ReflectionTestUtils.setField(paymentService, "defaultAmount", BigDecimal.ONE);
        when(paymentProvider.providerType()).thenReturn(com.barsege.paymentservice.entity.PaymentProvider.FAKE);
    }

    @Test
    void shouldPublishCompletedEventWhenProviderSucceeds() {
        when(paymentProvider.pay(any())).thenReturn(new PaymentResult(true, "tx-1", null));

        paymentService.processPayment(stockReservedEvent());

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentCompleted(any(PaymentCompletedEvent.class));
        verify(paymentEventPublisher, never()).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    void shouldPublishFailedEventWhenProviderFails() {
        when(paymentProvider.pay(any())).thenReturn(new PaymentResult(false, null, "declined"));

        paymentService.processPayment(stockReservedEvent());

        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentFailed(eventCaptor.capture());
        verify(paymentEventPublisher, never()).publishPaymentCompleted(any(PaymentCompletedEvent.class));
        assertThat(eventCaptor.getValue().reason()).isEqualTo("declined");
    }

    @Test
    void shouldPublishFailedEventWhenProviderThrowsException() {
        when(paymentProvider.pay(any())).thenThrow(new RuntimeException("provider unavailable"));

        paymentService.processPayment(stockReservedEvent());

        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentFailed(eventCaptor.capture());
        verify(paymentEventPublisher, never()).publishPaymentCompleted(any(PaymentCompletedEvent.class));
        assertThat(eventCaptor.getValue().reason()).contains("provider unavailable");
    }

    private StockReservedEvent stockReservedEvent() {
        return new StockReservedEvent(10L, "user-1", List.of());
    }
}
