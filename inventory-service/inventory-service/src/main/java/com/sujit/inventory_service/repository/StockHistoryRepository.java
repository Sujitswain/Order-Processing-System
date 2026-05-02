package com.sujit.inventory_service.repository;

import com.sujit.inventory_service.entity.StockHistory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID> {
    Optional<StockHistory> findByOrderIdAndProductId(UUID orderId, UUID productId);

    Optional<StockHistory> findByOrderIdAndProductIdAndTransactionId(UUID orderId, UUID productId, String transactionId);
}
