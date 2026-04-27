package com.sujit.order_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
    private List<OrderItemResponse> items;

}
