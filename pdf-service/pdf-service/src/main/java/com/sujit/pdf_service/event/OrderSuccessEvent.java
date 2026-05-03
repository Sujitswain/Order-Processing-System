package com.sujit.pdf_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessEvent {

    private UUID orderId;
    private UUID customerId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private Instant createdAt;
}