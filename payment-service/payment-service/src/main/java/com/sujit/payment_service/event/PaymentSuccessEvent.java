package com.sujit.payment_service.event;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentSuccessEvent {

    private UUID orderId;
    private String transactionId;
    private BigDecimal amount;
    private Instant processedAt;

}
