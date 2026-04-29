package com.barsege.paymentservice.service;

import com.barsege.paymentservice.entity.Payment;
import com.barsege.paymentservice.entity.PaymentProvider;
import com.barsege.paymentservice.entity.PaymentStatus;
import com.barsege.paymentservice.event.PaymentCompletedEvent;
import com.barsege.paymentservice.event.PaymentFailedEvent;
import com.barsege.paymentservice.event.StockReservedEvent;
import com.barsege.paymentservice.messaging.PaymentEventPublisher;
import com.barsege.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public void processPayment(StockReservedEvent event) {
        boolean paymentSuccess = event.orderId() % 2 == 0;
        // boolean paymentSuccess = false;
        
        if (paymentSuccess) {
            Payment payment = Payment.builder()
                    .orderId(event.orderId())
                    .userId(event.userId())
                    .amount(BigDecimal.ZERO)
                    .status(PaymentStatus.SUCCESS)
                    .provider(PaymentProvider.FAKE)
                    .build();

            paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentCompleted(
                    new PaymentCompletedEvent(
                            event.orderId(),
                            event.userId(),
                            BigDecimal.ZERO
                    )
            );
        } else {
            String reason = "Fake payment failed for testing";

            Payment payment = Payment.builder()
                    .orderId(event.orderId())
                    .userId(event.userId())
                    .amount(BigDecimal.ZERO)
                    .status(PaymentStatus.FAILED)
                    .provider(PaymentProvider.FAKE)
                    .failureReason(reason)
                    .build();

            paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentFailed(
                    new PaymentFailedEvent(
                            event.orderId(),
                            event.userId(),
                            reason,
                            event.items()
                    )
            );
        }
    }
}