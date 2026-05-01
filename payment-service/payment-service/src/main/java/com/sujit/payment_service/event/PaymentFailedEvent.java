package com.sujit.payment_service.event;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {

    private UUID orderId;
    private String reason;
    private Instant failedAt;

}
