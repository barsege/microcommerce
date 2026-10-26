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

import java.time.LocalDateTime;
import java.util.Currency;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentProvider paymentProvider;

    @Value("${payment.payment-method:MOCK}")
    private String paymentMethod;

    public void processPayment(StockReservedEvent event) {
        PaymentRequest request = new PaymentRequest(
                event.orderId(),
                event.userId(),
                event.totalAmount(),
                event.currencyCode(),
                paymentMethod,
                null,
                null,
                null,
                null,
                null
        );
        
        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .amount(event.totalAmount())
                .currency(Currency.getInstance(event.currencyCode()))
                .provider(paymentProvider.providerType())
                .status(PaymentStatus.PENDING)
                .build();

        
        payment = paymentRepository.save(payment);
        
        PaymentResult result;
        try {
            result = paymentProvider.pay(request);
        } catch (Exception exception) {
            log.error("Payment provider failed. orderId={}", event.orderId(), exception);
            payment.setStatus(PaymentStatus.UNKNOWN);
            payment.setFailureReason("Payment provider error: " + exception.getMessage());
            paymentRepository.save(payment);            
            return;
        }
        
        if (result.success()) {
        	
        	payment.setStatus(PaymentStatus.SUCCESS);
        	payment.setProviderTransactionId(result.transactionId());
        	payment.setCompletedAt(LocalDateTime.now());
            
            paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentCompleted(
                    new PaymentCompletedEvent(
                            event.orderId(),
                            event.userId(),
                            event.totalAmount(),
                            event.items()
                    )
            );
        } else {
            String reason = result.failureReason();

            payment.setStatus(PaymentStatus.FAILED);
        	payment.setProviderTransactionId(result.transactionId());
        	payment.setCompletedAt(LocalDateTime.now());
        	payment.setFailureReason(reason);

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
