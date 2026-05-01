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
public class PaymentResponse {

    private UUID orderId;
    private String transactionId;
    private BigDecimal amount;
    private String status;
    private String responsePayload;

}