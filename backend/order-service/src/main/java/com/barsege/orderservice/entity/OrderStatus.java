package com.barsege.orderservice.entity;

public enum OrderStatus {

	CREATED,
    STOCK_RESERVED,
    STOCK_RESERVATION_FAILED,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
