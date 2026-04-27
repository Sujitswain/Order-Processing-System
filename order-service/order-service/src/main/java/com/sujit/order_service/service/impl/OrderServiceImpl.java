package com.sujit.order_service.service.impl;

import com.sujit.order_service.dto.CreateOrderRequest;
import com.sujit.order_service.dto.OrderResponse;
import com.sujit.order_service.entity.OrderEntity;
import com.sujit.order_service.entity.OrderItemEntity;
import com.sujit.order_service.enums.OrderStatus;
import com.sujit.order_service.event.OrderCancelledEvent;
import com.sujit.order_service.event.OrderCreatedEvent;
import com.sujit.order_service.exception.OrderBadRequestException;
import com.sujit.order_service.repository.OrderRepository;
import com.sujit.order_service.service.OrderProducer;
import com.sujit.order_service.service.OrderService;
import com.sujit.order_service.utils.ApplicationUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sujit.order_service.utils.ApplicationUtil.*;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderServiceImpl(OrderRepository orderRepository, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        // create order entity
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.getCustomerId());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        // create order-item entity
        List<OrderItemEntity> items = request.getItems().stream().map(itemRequest -> {
            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        // set the order-item in order
        order.setItems(items);

        // persist in table
        OrderEntity saved = orderRepository.save(order);

        // create order event and publish to kafka-topic
        OrderCreatedEvent event = toOrderCreatedEvent(saved);
        orderProducer.publish("order-created", event);

        return mapToOrderResponse(saved);
    }

    @Override
    public OrderResponse getOrder(UUID orderId) {
        OrderEntity order = getOrderByOrderId(orderId);
        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByCustomer(UUID customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<OrderEntity> result = orderRepository.findByCustomerId(customerId, pageable);
        return result.map(ApplicationUtil::mapToOrderResponse);
    }

    @Transactional
    @Override
    public OrderResponse cancelOrder(UUID orderId) {
        OrderEntity order = getOrderByOrderId(orderId);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderBadRequestException("Order cannot be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // create an order cancel event and public it to kafka
        OrderCancelledEvent cancelledEvent = toOrderCancelledEvent(order, "Cancelled By Customer");
        orderProducer.publish("order-cancelled", cancelledEvent);

        return mapToOrderResponse(order);
    }

    @Transactional
    @Override
    public void markOrderCompleted(UUID orderId) {
        OrderEntity order = getOrderByOrderId(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void markOrderPaymentFailed(UUID orderId) {
        OrderEntity order = getOrderByOrderId(orderId);
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }

    public OrderEntity getOrderByOrderId(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderBadRequestException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
