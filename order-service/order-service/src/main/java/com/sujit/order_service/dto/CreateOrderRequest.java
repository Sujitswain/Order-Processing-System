package com.sujit.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull
    private UUID customerId;

    @NotNull
    private String customerEmail;

    @NotNull
    @Min(1)
    private BigDecimal totalAmount;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

}
