package com.sujit.inventory_service.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public class OrderCompletedEvent {

    private UUID orderId;
    private Instant completedAt;

}
