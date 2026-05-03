package com.sujit.inventory_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "products_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStock {

    @Id
    private UUID productId;

    // A human-readable code used by business teams to identify the item's type, size, or color.
    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reservedQuantity;

    @Version
    @Column(nullable = false)
    private long version;

    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }
}
