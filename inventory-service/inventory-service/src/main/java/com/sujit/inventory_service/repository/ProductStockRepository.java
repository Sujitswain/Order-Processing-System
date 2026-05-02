package com.sujit.inventory_service.repository;

import com.sujit.inventory_service.entity.ProductStock;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {
    List<ProductStock> findByQuantityLessThan(int threshold);
}
