package com.sujit.payment_service.dto;


import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private UUID productId;
    private int quantity;
    private BigDecimal price;

}