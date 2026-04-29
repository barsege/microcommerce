package com.barsege.paymentservice.messaging;

import com.barsege.paymentservice.config.RabbitMQConfig;
import com.barsege.paymentservice.event.PaymentCompletedEvent;
import com.barsege.paymentservice.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY,
                event
        );
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY,
                event
        );
    }
}