package com.sujit.inventory_service.event;

import lombok.*;

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
    private Instant processedAt;

}