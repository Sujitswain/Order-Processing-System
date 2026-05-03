package com.sujit.payment_service.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.sujit.payment_service.dto.CreatePaymentIntentRequest;
import com.sujit.payment_service.dto.PaymentIntentResponse;
import com.sujit.payment_service.dto.PaymentResponse;
import com.sujit.payment_service.dto.PaymentStatsResponse;
import com.sujit.payment_service.entity.PaymentEntity;
import com.sujit.payment_service.enums.PaymentStatus;
import com.sujit.payment_service.event.OrderCreatedEvent;
import com.sujit.payment_service.event.PaymentFailedEvent;
import com.sujit.payment_service.event.PaymentSuccessEvent;
import com.sujit.payment_service.exception.PaymentBadRequestException;
import com.sujit.payment_service.exception.PaymentInternalServerException;
import com.sujit.payment_service.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.sujit.payment_service.util.ApplicationUtil.mapToPaymentresponse;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public PaymentService(PaymentRepository paymentRepository, PaymentProducer paymentProducer, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        UUID orderId = event.getOrderId();
        log.info("Order created event received for order: {}. Creating initial payment record.", orderId);

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.warn("Payment record already exists for order: {}", orderId);
            return;
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(orderId);
        payment.setTransactionId(null);
        payment.setAmount(event.getTotalAmount());
        payment.setStatus(PaymentStatus.INITIALIZED.name());
        payment.setResponsePayload("{}");
        payment.setCreatedAt(Instant.now());

        paymentRepository.save(payment);
        log.info("Initial payment record created for order: {}", orderId);
    }

    public PaymentResponse getByOrderId(UUID orderId) {
        log.info("Retrieving payment for order: {}", orderId);
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment record not found"));

        return mapToPaymentresponse(payment);
    }

    public PaymentStatsResponse getStats() {
        long success = paymentRepository.countByStatus(PaymentStatus.SUCCESS.name());
        long failed = paymentRepository.countByStatus(PaymentStatus.FAILURE.name());
        PaymentStatsResponse stats = new PaymentStatsResponse();
        stats.setSuccessCount(success);
        stats.setFailedCount(failed);
        stats.setTotalPayments(success + failed);
        return stats;
    }

    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        log.info("Received Stripe webhook");
        log.debug("Stripe signature header: {}", sigHeader);
        try {
            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // Handle the event
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    try {
                        log.info("Handling payment_intent.succeeded event");
                        handlePaymentIntentSucceeded(event);
                    } catch (Exception e) {
                        log.error("Failed to handle payment_intent.succeeded: {}", e.getMessage());
                    }
                    break;
                case "payment_intent.payment_failed":
                    try {
                        log.info("Handling payment_intent.payment_failed event");
                        handlePaymentIntentFailed(event);
                    } catch (Exception e) {
                        log.error("Failed to handle payment_intent.payment_failed: {}", e.getMessage());
                    }
                    break;
                default:
                    log.info("Ignoring unhandled event type: {}", event.getType());
            }

        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature");
            throw new PaymentBadRequestException("Invalid Stripe signature");

        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            throw new PaymentBadRequestException("Webhook failed: " + e.getMessage());
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = getPaymentIntentFromEvent(event);
        String orderIdStr = paymentIntent.getMetadata().get("orderId");
        if (orderIdStr == null) {
            log.warn("Missing orderId in metadata");
            return;
        }

        UUID orderId = UUID.fromString(orderIdStr);
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentInternalServerException("Payment not found for order: " + orderId));

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("paymentIntentId", paymentIntent.getId());
            payload.put("status", PaymentStatus.SUCCESS.name());

            payment.setStatus(PaymentStatus.SUCCESS.name());
            payment.setTransactionId(paymentIntent.getId()); // Update with actual payment intent ID
            payment.setResponsePayload(objectMapper.writeValueAsString(payload));

            paymentRepository.save(payment);

            publishSuccess(payment);

            log.info("Payment SUCCESS for order {}", orderId);

        } catch (Exception e) {
            throw new PaymentInternalServerException(e.getMessage());
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = getPaymentIntentFromEvent(event);

        String orderIdStr = paymentIntent.getMetadata().get("orderId");
        if (orderIdStr == null) {
            return;
        }

        UUID orderId = UUID.fromString(orderIdStr);
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentInternalServerException("Payment not found for order: " + orderId));

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("paymentIntentId", paymentIntent.getId());
            payload.put("status", PaymentStatus.FAILURE.name());
            payload.put("error", paymentIntent.getLastPaymentError() != null
                            ? paymentIntent.getLastPaymentError().getMessage()
                            : "Unknown");

            payment.setStatus(PaymentStatus.FAILURE.name());
            payment.setResponsePayload(objectMapper.writeValueAsString(payload));

            paymentRepository.save(payment);
            publishFailure(orderId, "Payment failed");
            log.info("Payment FAILED for order {}", orderId);

        } catch (Exception e) {
            throw new PaymentInternalServerException(e.getMessage());
        }
    }

    private void publishSuccess(PaymentEntity payment) {
        log.info("Publishing payment success event for order: {}", payment.getOrderId());
        PaymentSuccessEvent event = new PaymentSuccessEvent();
        event.setOrderId(payment.getOrderId());
        event.setTransactionId(payment.getTransactionId());
        event.setAmount(payment.getAmount());
        event.setProcessedAt(Instant.now());
        paymentProducer.publish("payment-success", event);
    }

    private void publishFailure(UUID orderId, String reason) {
        log.info("Publishing payment failure event for order: {}", orderId);
        PaymentFailedEvent event = new PaymentFailedEvent();
        event.setOrderId(orderId);
        event.setReason(reason);
        event.setFailedAt(Instant.now());
        paymentProducer.publish("payment-failed", event);
    }

    private PaymentIntent getPaymentIntentFromEvent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return (PaymentIntent) deserializer.getObject().get();
        }

        try {
            log.warn("API version mismatch (Event: {}, SDK: {}). Using unsafe deserialization.",
                    event.getApiVersion(), Stripe.API_VERSION);
            return (PaymentIntent) deserializer.deserializeUnsafe();
        } catch (Exception e) {
            log.error("Deserialization failed for event: {}", event.getId());
            throw new PaymentInternalServerException("Deserialization failed for event: " + event.getId());
        }
    }

    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        log.info("Creating payment intent for order: {}", request.getOrderId());
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(request.getCurrency())
                    .putMetadata("orderId", request.getOrderId().toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            PaymentIntentResponse response = new PaymentIntentResponse();
            response.setPaymentIntentId(paymentIntent.getId());
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setStatus(paymentIntent.getStatus());

            log.info("Payment intent created successfully: {}", paymentIntent.getId());
            return response;

        } catch (StripeException e) {
            log.error("Failed to create payment intent for order {}: {}", request.getOrderId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create payment intent: " + e.getMessage());
        }
    }
}
