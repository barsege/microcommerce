package com.barsege.orderservice.service;

import com.barsege.orderservice.dto.request.CreateOrderItemRequest;
import com.barsege.orderservice.dto.request.CreateOrderRequest;
import com.barsege.orderservice.dto.response.OrderResponse;
import com.barsege.orderservice.entity.Order;
import com.barsege.orderservice.entity.OrderItem;
import com.barsege.orderservice.entity.OrderStatus;
import com.barsege.orderservice.event.OrderCreatedEvent;
import com.barsege.orderservice.messaging.OrderEventPublisher;
import com.barsege.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndPublishEvent() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            order.setStatus(OrderStatus.CREATED);
            return order;
        });

        OrderResponse response = orderService.createOrder("keycloak-sub-user-1", createOrderRequest());

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventPublisher).publishOrderCreated(eventCaptor.capture());
        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.totalAmount()).isEqualByComparingTo("100");
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(10L);
        assertThat(eventCaptor.getValue().userId()).isEqualTo("keycloak-sub-user-1");
        assertThat(eventCaptor.getValue().items()).hasSize(1);
    }

    @Test
    void shouldGetOrderById() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order()));

        OrderResponse response = orderService.getOrderById(10L, "user-1");

        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void shouldNotReturnOtherUsersOrderById() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order()));

        assertThatThrownBy(() -> orderService.getOrderById(10L, "user-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void shouldListOrdersByUserId() {
        when(orderRepository.findByUserId("user-1")).thenReturn(List.of(order()));

        List<OrderResponse> responses = orderService.getOrdersByUserId("user-1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo("user-1");
    }

    @Test
    void shouldKeepUserOrdersIsolatedBySubject() {
        when(orderRepository.findByUserId("keycloak-sub-user-1")).thenReturn(List.of(orderWithUser("keycloak-sub-user-1")));
        when(orderRepository.findByUserId("keycloak-sub-user-2")).thenReturn(List.of());

        List<OrderResponse> user1Orders = orderService.getOrdersByUserId("keycloak-sub-user-1");
        List<OrderResponse> user2Orders = orderService.getOrdersByUserId("keycloak-sub-user-2");

        assertThat(user1Orders).hasSize(1);
        assertThat(user1Orders.get(0).userId()).isEqualTo("keycloak-sub-user-1");
        assertThat(user2Orders).isEmpty();
    }

    private CreateOrderRequest createOrderRequest() {
        return new CreateOrderRequest(
                null,
                List.of(new CreateOrderItemRequest(1L, "Keyboard", BigDecimal.valueOf(50), 2))
        );
    }

    private Order order() {
        return orderWithUser("user-1");
    }

    private Order orderWithUser(String userId) {
        Order order = Order.builder()
                .id(10L)
                .userId(userId)
                .totalAmount(BigDecimal.valueOf(100))
                .status(OrderStatus.CREATED)
                .build();
        OrderItem item = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Keyboard")
                .unitPrice(BigDecimal.valueOf(50))
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(100))
                .order(order)
                .build();
        order.setItems(List.of(item));
        return order;
    }
}
