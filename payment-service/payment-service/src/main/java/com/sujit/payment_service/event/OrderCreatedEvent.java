package com.sujit.payment_service.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private Instant createdAt;

}