package com.sujit.inventory_service.event;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {

    private UUID orderId;
    private Instant completedAt;

}
