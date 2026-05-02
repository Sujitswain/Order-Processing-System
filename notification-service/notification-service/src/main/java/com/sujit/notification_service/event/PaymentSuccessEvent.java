package com.sujit.notification_service.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PaymentSuccessEvent {

    private UUID orderId;
    private String transactionId;
    private Instant processedAt;

}