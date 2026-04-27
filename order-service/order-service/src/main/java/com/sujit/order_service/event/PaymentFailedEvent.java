package com.sujit.order_service.event;

import lombok.*;

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

}
