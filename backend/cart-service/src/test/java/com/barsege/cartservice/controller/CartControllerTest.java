package com.barsege.cartservice.controller;

import com.barsege.cartservice.dto.response.CartItemResponse;
import com.barsege.cartservice.dto.response.CartResponse;
import com.barsege.cartservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void shouldAddItem() throws Exception {
        when(cartService.addItem(eq("keycloak-sub-user-1"), any())).thenReturn(cartResponse("keycloak-sub-user-1"));

        mockMvc.perform(post("/api/carts/items")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-sub-user-1")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": 1,
                                  "productName": "Keyboard",
                                  "unitPrice": 50,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Keyboard"));

        verify(cartService).addItem(eq("keycloak-sub-user-1"), any());
    }

    @Test
    void shouldGetCart() throws Exception {
        when(cartService.getCart("keycloak-sub-user-1")).thenReturn(cartResponse("keycloak-sub-user-1"));

        mockMvc.perform(get("/api/carts")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-sub-user-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("keycloak-sub-user-1"))
                .andExpect(jsonPath("$.totalAmount").value(50));

        verify(cartService).getCart("keycloak-sub-user-1");
    }

    private CartResponse cartResponse(String userId) {
        return new CartResponse(
                1L,
                userId,
                List.of(new CartItemResponse(1L, 1L, "Keyboard", BigDecimal.valueOf(50), 1, BigDecimal.valueOf(50))),
                BigDecimal.valueOf(50)
        );
    }
}
