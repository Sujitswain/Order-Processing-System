package com.sujit.inventory_service.service;

import com.sujit.inventory_service.client.OrderServiceClient;
import com.sujit.inventory_service.dto.InventoryDto;
import com.sujit.inventory_service.dto.OrderItemResponse;
import com.sujit.inventory_service.dto.OrderResponse;
import com.sujit.inventory_service.entity.ProductStock;
import com.sujit.inventory_service.entity.StockHistory;
import com.sujit.inventory_service.event.InventoryEvents;
import com.sujit.inventory_service.event.InventoryEvents.InventoryUpdatedEvent;
import com.sujit.inventory_service.event.InventoryEvents.OrderCancelledEvent;
import com.sujit.inventory_service.event.InventoryEvents.PaymentSuccessEvent;
import com.sujit.inventory_service.exception.InventoryBadRequestException;
import com.sujit.inventory_service.repository.ProductStockRepository;
import com.sujit.inventory_service.repository.StockHistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    public InventoryDto.InventoryResponse getInventory(UUID productId) {
        ProductStock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new InventoryBadRequestException(HttpStatus.NOT_FOUND, "Product stock not found"));
        return toResponse(stock);
    }

    @Transactional
    public InventoryDto.InventoryResponse restock(InventoryDto.RestockRequest request) {
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

    public List<InventoryDto.InventoryResponse> getLowStock(int threshold) {
        return stockRepository.findByQuantityLessThan(threshold).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        OrderResponse order = orderServiceClient.getOrder(event.getOrderId());
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InventoryBadRequestException(HttpStatus.BAD_REQUEST, "Order item details not found for order: " + event.getOrderId());
        }

        for (OrderItemResponse item : order.getItems()) {
            // Idempotency check: ensure this exact stock change was not already processed
            if (historyRepository.findByOrderIdAndProductIdAndTransactionId(event.getOrderId(), item.getProductId(), event.getTransactionId()).isPresent()) {
                continue;
            }
            ProductStock stock = stockRepository.findById(item.getProductId()).orElseThrow(
                    () -> new InventoryBadRequestException(HttpStatus.NOT_FOUND, "Stock not available for product: " + item.getProductId()));
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stockRepository.save(stock);

            StockHistory history = new StockHistory();
            history.setOrderId(event.getOrderId());
            history.setProductId(item.getProductId());
            history.setTransactionId(event.getTransactionId());
            history.setChangeAmount(-item.getQuantity());
            history.setCreatedAt(Instant.now());
            historyRepository.save(history);

            publishInventoryUpdated(stock, event.getOrderId(), -item.getQuantity());
        }
        InventoryEvents.OrderCompletedEvent orderCompletedEvent = new InventoryEvents.OrderCompletedEvent();
        orderCompletedEvent.setOrderId(event.getOrderId());
        orderCompletedEvent.setCompletedAt(Instant.now());
        inventoryProducer.publish("order-completed", orderCompletedEvent);
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
            stock.setQuantity(stock.getQuantity() + item.getQuantity());
            stockRepository.save(stock);

            StockHistory history = new StockHistory();
            history.setOrderId(event.getOrderId());
            history.setProductId(item.getProductId());
            history.setChangeAmount(item.getQuantity());
            history.setCreatedAt(Instant.now());
            historyRepository.save(history);

            publishInventoryUpdated(stock, event.getOrderId(), item.getQuantity());
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

    private InventoryDto.InventoryResponse toResponse(ProductStock stock) {
        InventoryDto.InventoryResponse response = new InventoryDto.InventoryResponse();
        response.setProductId(stock.getProductId());
        response.setSku(stock.getSku());
        response.setQuantity(stock.getQuantity());
        return response;
    }
}
