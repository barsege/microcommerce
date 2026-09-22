package com.barsege.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String userId;

    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private Currency currency;
    
    private String providerTransactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    private String failureReason;

    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}