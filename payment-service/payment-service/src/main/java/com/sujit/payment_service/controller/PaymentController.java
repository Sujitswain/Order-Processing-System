package com.sujit.payment_service.controller;

import com.stripe.model.checkout.Session;
import com.sujit.payment_service.dto.*;
import com.sujit.payment_service.service.OrderService;
import com.sujit.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment API", description = "APIs for handling payments with Stripe")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create Stripe checkout session for cart items
    @PostMapping("/create-checkout-session")
    public ResponseEntity<StripeResponse> createCheckoutSession(@RequestParam UUID orderId,
                                                                @RequestBody List<CheckoutItemDto> checkoutItemDtoList) {
        Session session = orderService.createSession(orderId, checkoutItemDtoList);
        StripeResponse stripeResponse = new StripeResponse(session.getId(), session.getUrl());
        return ResponseEntity.ok(stripeResponse);
    }

    // TODO: Only for custom payment (future)
    // Create payment intent for frontend
    // NOTE: This is an alternative Stripe flow, not required if you already use checkout session.
    // Checkout session is enough for most use cases and handles the hosted payment page.
    // PaymentIntent is useful when the frontend wants a custom payment UI using Stripe.js or Stripe Elements.
    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        PaymentIntentResponse response = paymentService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    // Stripe webhook endpoint
    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature header");
        }

        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("Webhook processed successfully");
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable UUID orderId) {
        var result = paymentService.getByOrderId(orderId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsResponse> getStats() {
        return ResponseEntity.ok(paymentService.getStats());
    }
}
