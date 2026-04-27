package com.sujit.order_service.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SuperBuilder
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
    private List<OrderItemEvent> items;

}

