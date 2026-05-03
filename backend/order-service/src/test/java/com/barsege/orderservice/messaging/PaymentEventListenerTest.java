package com.barsege.orderservice.messaging;

import com.barsege.orderservice.entity.Order;
import com.barsege.orderservice.entity.OrderStatus;
import com.barsege.orderservice.event.PaymentCompletedEvent;
import com.barsege.orderservice.event.PaymentFailedEvent;
import com.barsege.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentEventListenerTest {

    private OrderRepository orderRepository;
    private PaymentEventListener listener;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        listener = new PaymentEventListener(orderRepository);
    }

    @Test
    void shouldMarkOrderAsPaidWhenPaymentCompleted() {
        Order order = order();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        listener.handlePaymentCompleted(new PaymentCompletedEvent(10L, "user-1", BigDecimal.valueOf(100)));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldMarkOrderAsPaymentFailedWhenPaymentFailed() {
        Order order = order();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        listener.handlePaymentFailed(new PaymentFailedEvent(10L, "user-1", "declined"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        verify(orderRepository).save(order);
    }

    private Order order() {
        return Order.builder()
                .id(10L)
                .userId("user-1")
                .totalAmount(BigDecimal.valueOf(100))
                .status(OrderStatus.CREATED)
                .build();
    }
}
