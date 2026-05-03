package com.sujit.order_service.utils;

import com.sujit.order_service.dto.*;
import com.sujit.order_service.entity.Customer;
import com.sujit.order_service.entity.OrderEntity;
import com.sujit.order_service.entity.OrderItemEntity;
import com.sujit.order_service.entity.Product;
import com.sujit.order_service.enums.OrderStatus;
import com.sujit.order_service.event.OrderCancelledEvent;
import com.sujit.order_service.event.OrderCreatedEvent;
import com.sujit.order_service.event.OrderSuccessEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ApplicationUtil {

    public static CustomerResponse mapToCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .country(customer.getCountry())
                .zipCode(customer.getZipCode())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    public static ProductResponse mapToProductResponse(Product product, int quantity) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .quantity(quantity)
                .build();
    }

    public static OrderResponse mapToOrderResponse(OrderEntity order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems().stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductId(item.getProductId());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            return itemResponse;
        }).toList());
        return response;
    }

    public static OrderCreatedEvent toOrderCreatedEvent(OrderEntity order) {
        return OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public static OrderSuccessEvent toOrderSuccessEvent(OrderEntity order) {
        return OrderSuccessEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public static OrderCancelledEvent toOrderCancelledEvent(OrderEntity order, String reason) {
        return OrderCancelledEvent.builder()
                .orderId(order.getId())
                .reason(reason)
                .build();
    }

    public static OrderEntity buildOrderEntity(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.getCustomerId());
        order.setCustomerEmail(request.getCustomerEmail());
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

        // Calculate totalAmount from order items
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        order.setTotalAmount(totalAmount);
        return order;
    }

}
