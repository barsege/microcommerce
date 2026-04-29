package com.barsege.productservice.messaging;

import com.barsege.productservice.config.RabbitMQConfig;
import com.barsege.productservice.event.PaymentFailedEvent;
import com.barsege.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFailedEventListener {

    private final ProductService productService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {

        System.out.println("ROLLBACK TRIGGERED");
        System.out.println("Items: " + event.items());

        event.items().forEach(item ->
                productService.releaseStock(item.productId(), item.quantity())
        );
    }
}