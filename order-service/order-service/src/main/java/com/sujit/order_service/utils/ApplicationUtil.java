package com.sujit.order_service.utils;

import com.sujit.order_service.dto.OrderItemResponse;
import com.sujit.order_service.dto.OrderResponse;
import com.sujit.order_service.dto.ProductResponse;
import com.sujit.order_service.entity.OrderEntity;
import com.sujit.order_service.entity.Product;
import com.sujit.order_service.event.OrderCancelledEvent;
import com.sujit.order_service.event.OrderCreatedEvent;
import com.sujit.order_service.event.OrderSuccessEvent;

public class ApplicationUtil {

    public static ProductResponse mapToResponse(Product product, int quantity) {
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

}
