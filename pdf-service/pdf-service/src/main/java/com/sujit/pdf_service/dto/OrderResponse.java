package com.sujit.pdf_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {

    private UUID orderId;
    private UUID customerId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
    private List<OrderItemResponse> items;

}