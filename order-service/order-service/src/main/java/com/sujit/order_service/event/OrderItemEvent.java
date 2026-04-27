package com.sujit.order_service.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {

    private UUID productId;
    private int quantity;
    private BigDecimal price;

}
