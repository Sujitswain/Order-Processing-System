package com.sujit.payment_service.dto;

import lombok.*;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemDto {

    private String productName;
    private int quantity;
    private double price;

}