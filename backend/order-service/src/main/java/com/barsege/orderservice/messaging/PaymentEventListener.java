package com.barsege.orderservice.messaging;

import com.barsege.orderservice.config.RabbitMQConfig;
import com.barsege.orderservice.entity.Order;
import com.barsege.orderservice.entity.OrderStatus;
import com.barsege.orderservice.event.PaymentCompletedEvent;
import com.barsege.orderservice.event.PaymentFailedEvent;
import com.barsege.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }
}