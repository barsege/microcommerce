package com.barsege.productservice.messaging;

import com.barsege.productservice.config.RabbitMQConfig;
import com.barsege.productservice.event.PaymentCompletedEvent;
import com.barsege.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedEventListener {

    private final ProductService productService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        System.out.println("STOCK CONFIRM TRIGGERED");
        System.out.println("Items: " + event.items());

        event.items().forEach(item ->
                productService.confirmStock(item.productId(), item.quantity())
        );
    }
}