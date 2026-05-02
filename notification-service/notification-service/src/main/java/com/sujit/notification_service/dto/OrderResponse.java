package com.sujit.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {

    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
    private List<OrderItemResponse> items;
    private String customerEmail;

}
