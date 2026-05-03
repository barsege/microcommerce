package com.barsege.orderservice.controller;

import com.barsege.orderservice.dto.response.OrderItemResponse;
import com.barsege.orderservice.dto.response.OrderResponse;
import com.barsege.orderservice.entity.OrderStatus;
import com.barsege.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrderForAuthenticatedUser() throws Exception {
        when(orderService.createOrder(eq("keycloak-sub-user-1"), any())).thenReturn(orderResponse("keycloak-sub-user-1"));

        mockMvc.perform(post("/api/orders")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-sub-user-1")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 1,
                                      "productName": "Keyboard",
                                      "unitPrice": 50,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("keycloak-sub-user-1"));

        verify(orderService).createOrder(eq("keycloak-sub-user-1"), any());
    }

    @Test
    void shouldListOrdersForAuthenticatedUser() throws Exception {
        when(orderService.getOrdersByUserId("keycloak-sub-user-1")).thenReturn(List.of(orderResponse("keycloak-sub-user-1")));

        mockMvc.perform(get("/api/orders")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-sub-user-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("keycloak-sub-user-1"));

        verify(orderService).getOrdersByUserId("keycloak-sub-user-1");
    }

    @Test
    void shouldGetOrderDetailForAuthenticatedUser() throws Exception {
        when(orderService.getOrderById(10L, "keycloak-sub-user-1")).thenReturn(orderResponse("keycloak-sub-user-1"));

        mockMvc.perform(get("/api/orders/10")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-sub-user-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(10));

        verify(orderService).getOrderById(10L, "keycloak-sub-user-1");
    }

    private OrderResponse orderResponse(String userId) {
        return new OrderResponse(
                10L,
                userId,
                BigDecimal.valueOf(100),
                OrderStatus.CREATED,
                List.of(new OrderItemResponse(1L, 1L, "Keyboard", BigDecimal.valueOf(50), 2, BigDecimal.valueOf(100)))
        );
    }
}
