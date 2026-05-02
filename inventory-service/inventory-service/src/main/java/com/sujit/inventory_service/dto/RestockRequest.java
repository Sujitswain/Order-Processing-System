package com.sujit.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestockRequest {

    @NotNull
    private UUID productId;

    private String sku;

    @Min(1)
    private int quantity;

}
