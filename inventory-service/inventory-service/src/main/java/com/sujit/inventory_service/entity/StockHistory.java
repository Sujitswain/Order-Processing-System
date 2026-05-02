package com.sujit.inventory_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_history", uniqueConstraints = @UniqueConstraint(columnNames = {"orderId", "productId", "transactionId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private int changeAmount;

    @Column(nullable = false)
    private Instant createdAt;

}
