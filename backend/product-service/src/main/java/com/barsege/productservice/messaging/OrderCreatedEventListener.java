package com.barsege.productservice.messaging;

import com.barsege.productservice.config.RabbitMQConfig;
import com.barsege.productservice.event.OrderCreatedEvent;
import com.barsege.productservice.event.StockItemEvent;
import com.barsege.productservice.event.StockReservationFailedEvent;
import com.barsege.productservice.event.StockReservedEvent;
import com.barsege.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final ProductService productService;
    private final StockEventPublisher stockEventPublisher;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            event.items().forEach(item ->
                    productService.reserveStock(item.productId(), item.quantity())
            );

            stockEventPublisher.publishStockReserved(
                    new StockReservedEvent(event.orderId(), event.userId(), event.items()
                            .stream()
                            .map(item -> new StockItemEvent(
                                    item.productId(),
                                    item.quantity()
                            ))
                            .toList())
            );

        } catch (Exception exception) {
            stockEventPublisher.publishStockReservationFailed(
                    new StockReservationFailedEvent(
                            event.orderId(),
                            event.userId(),
                            exception.getMessage()
                    )
            );
        }
    }
}