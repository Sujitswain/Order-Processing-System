package com.sujit.order_service.service;

import com.sujit.order_service.dto.CreateOrderRequest;
import com.sujit.order_service.dto.OrderResponse;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(UUID orderId);

    Page<OrderResponse> getOrdersByCustomer(UUID customerId, int page, int size);

    OrderResponse cancelOrder(UUID orderId);

    void markOrderCompleted(UUID orderId);

    void markOrderPaymentFailed(UUID orderId);
}

