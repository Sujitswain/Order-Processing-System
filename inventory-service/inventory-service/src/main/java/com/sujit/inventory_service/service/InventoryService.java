package com.sujit.inventory_service.service;

import com.sujit.inventory_service.client.OrderServiceClient;
import com.sujit.inventory_service.dto.InventoryResponse;
import com.sujit.inventory_service.dto.OrderItemResponse;
import com.sujit.inventory_service.dto.OrderResponse;
import com.sujit.inventory_service.dto.RestockRequest;
import com.sujit.inventory_service.entity.ProductStock;
import com.sujit.inventory_service.entity.StockHistory;
import com.sujit.inventory_service.event.InventoryUpdatedEvent;
import com.sujit.inventory_service.event.OrderCancelledEvent;
import com.sujit.inventory_service.event.OrderCompletedEvent;
import com.sujit.inventory_service.event.OrderCreatedEvent;
import com.sujit.inventory_service.event.PaymentSuccessEvent;
import com.sujit.inventory_service.exception.InventoryBadRequestException;
import com.sujit.inventory_service.repository.ProductStockRepository;
import com.sujit.inventory_service.repository.StockHistoryRepository;
import com.sujit.inventory_service.util.ApplicationUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.sujit.inventory_service.util.ApplicationUtil.toResponse;

@Service
public class InventoryService {

    private final ProductStockRepository stockRepository;
    private final StockHistoryRepository historyRepository;
    private final InventoryProducer inventoryProducer;
    private final OrderServiceClient orderServiceClient;

    public InventoryService(ProductStockRepository stockRepository,
                            StockHistoryRepository historyRepository,
                            InventoryProducer inventoryProducer,
                            OrderServiceClient orderServiceClient) {
        this.stockRepository = stockRepository;
        this.historyRepository = historyRepository;
        this.inventoryProducer = inventoryProducer;
        this.orderServiceClient = orderServiceClient;
    }

    public InventoryResponse getInventory(UUID productId) {
        ProductStock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new InventoryBadRequestException(HttpStatus.NOT_FOUND, "Product stock not found"));
        return toResponse(stock);
    }

    @Transactional
    public InventoryResponse restock(RestockRequest request) {
        ProductStock stock = stockRepository.findById(request.getProductId()).orElseGet(() -> {
            ProductStock newStock = new ProductStock();
            newStock.setProductId(request.getProductId());
            newStock.setSku(request.getSku());
            newStock.setQuantity(0);
            return newStock;
        });
        stock.setSku(request.getSku() != null ? request.getSku() : stock.getSku());
        stock.setQuantity(stock.getQuantity() + request.getQuantity());
        ProductStock saved = stockRepository.save(stock);

        InventoryUpdatedEvent event = new InventoryUpdatedEvent();
        event.setProductId(saved.getProductId());
        event.setOrderId(null);
        event.setQuantityChange(request.getQuantity());
        event.setRemainingQuantity(saved.getQuantity());
        event.setUpdatedAt(Instant.now());
        inventoryProducer.publish("inventory-updated", event);

        return toResponse(saved);
    }

    public List<InventoryResponse> getLowStock(int threshold) {
        return stockRepository.findByQuantityLessThan(threshold).stream().map(ApplicationUtil::toResponse).toList();
    }

    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        logInventoryEvent(event.getOrderId(), "RESERVE", 0);
    }

    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        logInventoryEvent(event.getOrderId(), event.getTransactionId(), -1);
        OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent();
        orderCompletedEvent.setOrderId(event.getOrderId());
        orderCompletedEvent.setCompletedAt(Instant.now());
        inventoryProducer.publish("order-completed", orderCompletedEvent);
    }

    /**
     * Common method to log inventory events for order lifecycle.
     * transactionType: "RESERVE" for reservation, event.transactionId() for payment finalization
     */
    private void logInventoryEvent(UUID orderId, String transactionType, int changeAmountMultiplier) {
        OrderResponse order = orderServiceClient.getOrder(orderId);
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InventoryBadRequestException(HttpStatus.BAD_REQUEST, "Order item details not found for order: " + orderId);
        }

        for (OrderItemResponse item : order.getItems()) {
            // Idempotency check: don't process same transaction twice
            if (historyRepository.findByOrderIdAndProductIdAndTransactionId(orderId, item.getProductId(), transactionType).isPresent()) {
                continue;
            }

            ProductStock stock = stockRepository.findById(item.getProductId()).orElseThrow(
                    () -> new InventoryBadRequestException(HttpStatus.NOT_FOUND, "Stock not available for product: " + item.getProductId()));

            StockHistory history = new StockHistory();
            history.setOrderId(orderId);
            history.setProductId(item.getProductId());
            history.setTransactionId(transactionType);
            history.setChangeAmount(item.getQuantity() * changeAmountMultiplier);
            history.setCreatedAt(Instant.now());
            historyRepository.save(history);

            int changeAmount = changeAmountMultiplier == 0 ? 0 : item.getQuantity() * changeAmountMultiplier;
            publishInventoryUpdated(stock, orderId, changeAmount);
        }
    }

    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        OrderResponse order = orderServiceClient.getOrder(event.getOrderId());
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InventoryBadRequestException(HttpStatus.BAD_REQUEST, "Order item details not found for order: " + event.getOrderId());
        }

        for (OrderItemResponse item : order.getItems()) {
            if (historyRepository.findByOrderIdAndProductId(event.getOrderId(), item.getProductId()).isEmpty()) {
                continue;
            }
            ProductStock stock = stockRepository.findById(item.getProductId()).orElseGet(() -> {
                ProductStock fallback = new ProductStock();
                fallback.setProductId(item.getProductId());
                fallback.setSku("unknown");
                fallback.setQuantity(0);
                return fallback;
            });
            // Release is handled by order-service, just log history
            StockHistory history = new StockHistory();
            history.setOrderId(event.getOrderId());
            history.setProductId(item.getProductId());
            history.setChangeAmount(item.getQuantity()); // Released
            history.setCreatedAt(Instant.now());
            historyRepository.save(history);

            publishInventoryUpdated(stock, event.getOrderId(), item.getQuantity()); // Notify with change
        }
    }

    private void publishInventoryUpdated(ProductStock stock, UUID orderId, int changeAmount) {
        InventoryUpdatedEvent event = new InventoryUpdatedEvent();
        event.setProductId(stock.getProductId());
        event.setOrderId(orderId);
        event.setQuantityChange(changeAmount);
        event.setRemainingQuantity(stock.getQuantity());
        event.setUpdatedAt(Instant.now());
        inventoryProducer.publish("inventory-updated", event);
    }

}
