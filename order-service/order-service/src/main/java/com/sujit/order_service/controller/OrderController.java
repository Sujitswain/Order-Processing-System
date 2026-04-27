package com.sujit.order_service.controller;

import com.sujit.order_service.dto.CreateOrderRequest;
import com.sujit.order_service.dto.OrderResponse;
import com.sujit.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        var order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID orderId) {
        var order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = orderService.getOrdersByCustomer(customerId, page, size);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId) {
        var order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }
}
