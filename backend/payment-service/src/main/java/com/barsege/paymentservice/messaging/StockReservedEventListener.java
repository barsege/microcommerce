package com.barsege.paymentservice.messaging;

import com.barsege.paymentservice.config.RabbitMQConfig;
import com.barsege.paymentservice.event.StockReservedEvent;
import com.barsege.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockReservedEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_RESERVED_QUEUE)
    public void handleStockReserved(StockReservedEvent event) {
        paymentService.processPayment(event);
    }
}