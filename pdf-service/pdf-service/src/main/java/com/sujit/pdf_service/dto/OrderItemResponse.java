package com.sujit.pdf_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {

    private UUID productId;
    private int quantity;
    private BigDecimal price;

}