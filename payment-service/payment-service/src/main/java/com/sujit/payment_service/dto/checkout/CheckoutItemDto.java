package com.sujit.payment_service.dto.checkout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutItemDto {

    private String productName;
    private int quantity;
    private double price;
    private long productId;
    private int userId;

    public CheckoutItemDto() {}

    public CheckoutItemDto(String productName, int quantity, double price, long productId, int userId) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.productId = productId;
        this.userId = userId;
    }

}