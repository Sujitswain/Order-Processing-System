package com.sujit.order_service.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@SuperBuilder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessEvent {

    private UUID orderId;
    private UUID customerId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private Instant createdAt;
}