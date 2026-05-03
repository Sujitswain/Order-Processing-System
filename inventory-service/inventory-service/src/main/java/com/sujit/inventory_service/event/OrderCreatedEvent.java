package com.sujit.inventory_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private Instant createdAt;

}
