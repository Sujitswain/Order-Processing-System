package com.sujit.notification_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private UUID productId;
    private int quantity;
    private BigDecimal price;

}
