package com.barsege.productservice.mapper;

import com.barsege.productservice.dto.response.ProductResponse;
import com.barsege.productservice.entity.Product;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        int availableStock = product.getStock() - product.getReservedStock();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getReservedStock(),
                availableStock,
                product.getCategory(),
                product.getImageUrl()
        );
    }
}
