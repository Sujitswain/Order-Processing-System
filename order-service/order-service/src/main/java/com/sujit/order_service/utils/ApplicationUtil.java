package com.sujit.order_service.utils;

import com.sujit.order_service.dto.OrderItemResponse;
import com.sujit.order_service.dto.OrderResponse;
import com.sujit.order_service.entity.OrderEntity;
import com.sujit.order_service.event.OrderCancelledEvent;
import com.sujit.order_service.event.OrderCreatedEvent;
import com.sujit.order_service.event.OrderItemEvent;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationUtil {

    public static OrderResponse mapToOrderResponse(OrderEntity order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomerId());
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

    private static List<OrderItemEvent> mapToEventItems(OrderEntity order) {
        return order.getItems().stream()
                .map(item -> OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    public static OrderCreatedEvent toOrderCreatedEvent(OrderEntity order) {
        return OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(mapToEventItems(order))
                .build();
    }

    public static OrderCancelledEvent toOrderCancelledEvent(OrderEntity order, String reason) {
        return OrderCancelledEvent.builder()
                .orderId(order.getId())
                .reason(reason)
                .items(mapToEventItems(order))
                .build();
    }

}
