package com.sujit.notification_service.event;

import java.time.Instant;
import java.util.UUID;

public class PaymentFailedEvent {
    private UUID orderId;
    private String reason;
    private Instant failedAt;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }
}