package com.sujit.inventory_service.event;

import lombok.*;

import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private UUID orderId;
    private String reason;

}