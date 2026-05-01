package com.sujit.order_service.event;

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
public class PaymentSuccessEvent {

    private UUID orderId;
    private String transactionId;
    private BigDecimal amount;
    private Instant processedAt;
}
