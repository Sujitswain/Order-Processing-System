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
public class InventoryUpdatedEvent {

    private UUID productId;
    private UUID orderId;
    private int quantityChange;
    private int remainingQuantity;
    private Instant updatedAt;

}