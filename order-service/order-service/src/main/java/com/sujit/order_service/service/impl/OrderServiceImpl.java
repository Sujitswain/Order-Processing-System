package com.sujit.order_service.service.impl;

import com.sujit.order_service.dto.CreateOrderRequest;
import com.sujit.order_service.dto.OrderResponse;
import com.sujit.order_service.entity.OrderEntity;
import com.sujit.order_service.entity.OrderItemEntity;
import com.sujit.order_service.entity.ProductStock;
import com.sujit.order_service.enums.OrderStatus;
import com.sujit.order_service.event.OrderCancelledEvent;
import com.sujit.order_service.event.OrderCreatedEvent;
import com.sujit.order_service.event.OrderSuccessEvent;
import com.sujit.order_service.exception.OrderBadRequestException;
import com.sujit.order_service.repository.OrderRepository;
import com.sujit.order_service.repository.ProductStockRepository;
import com.sujit.order_service.service.OrderProducer;
import com.sujit.order_service.service.OrderService;
import com.sujit.order_service.utils.ApplicationUtil;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.sujit.order_service.utils.ApplicationUtil.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final ProductStockRepository productStockRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderProducer orderProducer,
                            ProductStockRepository productStockRepository) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
        this.productStockRepository = productStockRepository;
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // validate inventory for each item in the order
        request.getItems().forEach(itemRequest -> {
            validateInventory(itemRequest.getProductId(), itemRequest.getQuantity());
        });

        // create order entity
        OrderEntity order = buildOrderEntity(request);

        // persist in table
        OrderEntity saved = orderRepository.save(order);

        // create order event and publish to kafka-topic
        OrderCreatedEvent event = toOrderCreatedEvent(saved);
        log.info("Publishing order created event for order: {}", saved.getId());
        orderProducer.publish("order-created", event);

        return mapToOrderResponse(saved);
    }

    @Override
    public OrderResponse getOrder(UUID orderId) {
        log.info("Retrieving order: {}", orderId);
        OrderEntity order = getOrderByOrderId(orderId);

        log.info("Order retrieved: id={}, customerId={}, status={}", order.getId(), order.getCustomerId(), order.getStatus());
        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByCustomer(UUID customerId, int page, int size) {
        log.info("Retrieving orders for customer: {} page: {} size: {}", customerId, page, size);
        Pageable pageable = PageRequest.of(page, size);

        Page<OrderEntity> result = orderRepository.findByCustomerId(customerId, pageable);

        log.info("Retrieved orders for customer {}: count={}", customerId, result.getNumberOfElements());
        return result.map(ApplicationUtil::mapToOrderResponse);
    }

    @Transactional
    @Override
    public OrderResponse cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);
        OrderEntity order = getOrderByOrderId(orderId);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderBadRequestException("Order cannot be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // create an order cancel event and public it to kafka
        OrderCancelledEvent cancelledEvent = toOrderCancelledEvent(order, "Cancelled By Customer");
        log.info("Publishing order cancelled event for order: {}", orderId);
        orderProducer.publish("order-cancelled", cancelledEvent);

        log.info("Order cancelled: id={}, customerId={}", order.getId(), order.getCustomerId());
        return mapToOrderResponse(order);
    }

    @Transactional
    @Override
    public void markOrderCompleted(UUID orderId) {
        log.info("Marking order as completed: {}", orderId);
        OrderEntity order = getOrderByOrderId(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        OrderEntity saved = orderRepository.save(order);

        // Send order success event for PDF generation
        OrderSuccessEvent successEvent = toOrderSuccessEvent(saved);
        log.info("Publishing order success event for order: {}", saved.getId());
        orderProducer.publish("order-success", successEvent);

        log.info("Marking order as completed: id={}, customerId={}", order.getId(), order.getCustomerId());
    }

    @Transactional
    @Override
    public void markOrderPaymentFailed(UUID orderId) {
        log.info("Marking order as payment failed: {}", orderId);
        OrderEntity order = getOrderByOrderId(orderId);
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        log.info("Marking order as payment failed: id={}, customerId={}", order.getId(), order.getCustomerId());
    }

    public OrderEntity getOrderByOrderId(UUID orderId) {
        log.debug("Fetching order entity for: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderBadRequestException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void validateInventory(UUID productId, int quantity) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                ProductStock stock = productStockRepository.findByProductId(productId)
                        .orElseThrow(() -> new OrderBadRequestException("Product not found: " + productId));
                if (stock.getQuantity() < quantity) {
                    log.error("Product {} has insufficient inventory. Available={}, requested={}", productId, stock.getQuantity(), quantity);
                    throw new OrderBadRequestException(String.format(
                            "Product %s has insufficient inventory. Available=%d, requested=%d",
                            productId,
                            stock.getQuantity(),
                            quantity));
                }
                return;
            } catch (OptimisticLockException ex) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("Failed to validate stock for product {}: too many concurrent updates. Please try again.", productId);
                    throw new OrderBadRequestException("Stock validation conflict: too many concurrent updates. Please try again.");
                }
                log.warn("Optimistic lock conflict for product {}. Retry {}/{}", productId, retryCount, maxRetries);
                try {
                    Thread.sleep(100L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new OrderBadRequestException("Stock validation interrupted");
                }
            }
        }
    }

    private OrderEntity buildOrderEntity(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.getCustomerId());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        List<OrderItemEntity> items = request.getItems().stream()
                .map(itemRequest -> OrderItemEntity.builder()
                        .productId(itemRequest.getProductId())
                        .quantity(itemRequest.getQuantity())
                        .price(itemRequest.getPrice())
                        .order(order)
                        .build())
                .toList();

        order.setItems(items);
        return order;
    }
}
