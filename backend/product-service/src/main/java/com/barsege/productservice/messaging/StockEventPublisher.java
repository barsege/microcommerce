package com.barsege.productservice.messaging;

import com.barsege.productservice.config.RabbitMQConfig;
import com.barsege.productservice.event.StockReservationFailedEvent;
import com.barsege.productservice.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockReserved(StockReservedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STOCK_EXCHANGE,
                RabbitMQConfig.STOCK_RESERVED_ROUTING_KEY,
                event
        );
    }

    public void publishStockReservationFailed(StockReservationFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STOCK_EXCHANGE,
                RabbitMQConfig.STOCK_RESERVATION_FAILED_ROUTING_KEY,
                event
        );
    }
}