package com.barsege.productservice.controller;

import com.barsege.productservice.dto.response.ProductResponse;
import com.barsege.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldListProducts() throws Exception {
        when(productService.getProducts(any())).thenReturn(new PageImpl<>(
                List.of(response()),
                PageRequest.of(0, 10),
                1
        ));

        mockMvc.perform(get("/api/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Keyboard"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.availableStock").value(8));
    }

    private ProductResponse response() {
        return new ProductResponse(
                1L,
                "Keyboard",
                "Mechanical",
                BigDecimal.valueOf(100),
                10,
                2,
                8,
                "Accessories",
                "image"
        );
    }
}
