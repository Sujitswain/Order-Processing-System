package com.sujit.order_service.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@SuperBuilder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private UUID orderId;
    private String reason;
    private List<OrderItemEvent> items;

}