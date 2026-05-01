package com.sujit.payment_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    @Min(1)
    private BigDecimal amount;

    @NotNull
    private String currency = "usd";

}