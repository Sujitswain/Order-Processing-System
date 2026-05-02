package com.sujit.inventory_service.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InventoryEvents {

    public static class PaymentSuccessEvent {
        private UUID orderId;
        private String transactionId;
        private Instant processedAt;

        public UUID getOrderId() {
            return orderId;
        }

        public void setOrderId(UUID orderId) {
            this.orderId = orderId;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public Instant getProcessedAt() {
            return processedAt;
        }

        public void setProcessedAt(Instant processedAt) {
            this.processedAt = processedAt;
        }
    }

    public static class OrderCancelledEvent {
        private UUID orderId;
        private String reason;

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
    }

    public static class InventoryUpdatedEvent {
        private UUID productId;
        private UUID orderId;
        private int quantityChange;
        private int remainingQuantity;
        private Instant updatedAt;

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public void setOrderId(UUID orderId) {
            this.orderId = orderId;
        }

        public int getQuantityChange() {
            return quantityChange;
        }

        public void setQuantityChange(int quantityChange) {
            this.quantityChange = quantityChange;
        }

        public int getRemainingQuantity() {
            return remainingQuantity;
        }

        public void setRemainingQuantity(int remainingQuantity) {
            this.remainingQuantity = remainingQuantity;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class OrderCompletedEvent {
        private UUID orderId;
        private Instant completedAt;

        public UUID getOrderId() {
            return orderId;
        }

        public void setOrderId(UUID orderId) {
            this.orderId = orderId;
        }

        public Instant getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
        }
    }
}
