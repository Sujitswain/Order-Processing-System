package com.sujit.order_service.repository;

import com.sujit.order_service.entity.ProductStock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {

    Optional<ProductStock> findByProductId(UUID productId);
}
