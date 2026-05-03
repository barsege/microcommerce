package com.barsege.paymentservice.service;

import com.barsege.paymentservice.dto.PaymentRequest;
import com.barsege.paymentservice.dto.PaymentResult;
import com.barsege.paymentservice.entity.Payment;
import com.barsege.paymentservice.entity.PaymentStatus;
import com.barsege.paymentservice.event.PaymentCompletedEvent;
import com.barsege.paymentservice.event.PaymentFailedEvent;
import com.barsege.paymentservice.event.StockReservedEvent;
import com.barsege.paymentservice.messaging.PaymentEventPublisher;
import com.barsege.paymentservice.provider.PaymentProvider;
import com.barsege.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentProvider paymentProvider;

    @Value("${payment.currency:TRY}")
    private String currency;

    @Value("${payment.payment-method:MOCK}")
    private String paymentMethod;

    @Value("${payment.default-amount:1.00}")
    private BigDecimal defaultAmount;

    public void processPayment(StockReservedEvent event) {
        BigDecimal amount = defaultAmount;
        PaymentRequest request = new PaymentRequest(
                event.orderId(),
                event.userId(),
                amount,
                currency,
                paymentMethod,
                null,
                null,
                null,
                null,
                null
        );
        PaymentResult result;
        try {
            result = paymentProvider.pay(request);
        } catch (Exception exception) {
            log.error("Payment provider failed. orderId={}", event.orderId(), exception);
            result = new PaymentResult(false, null, "Payment provider error: " + exception.getMessage());
        }
        
        if (result.success()) {
            Payment payment = Payment.builder()
                    .orderId(event.orderId())
                    .userId(event.userId())
                    .amount(amount)
                    .status(PaymentStatus.SUCCESS)
                    .provider(paymentProvider.providerType())
                    .build();

            paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentCompleted(
                    new PaymentCompletedEvent(
                            event.orderId(),
                            event.userId(),
                            amount,
                            event.items()
                    )
            );
        } else {
            String reason = result.failureReason();

            Payment payment = Payment.builder()
                    .orderId(event.orderId())
                    .userId(event.userId())
                    .amount(amount)
                    .status(PaymentStatus.FAILED)
                    .provider(paymentProvider.providerType())
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
