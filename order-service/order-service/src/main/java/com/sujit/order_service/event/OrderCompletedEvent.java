package com.sujit.order_service.event;

import lombok.*;

import java.time.Instant;
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