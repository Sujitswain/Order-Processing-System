package com.sujit.inventory_service.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private UUID productId;
    private String sku;
    private int quantity;

}