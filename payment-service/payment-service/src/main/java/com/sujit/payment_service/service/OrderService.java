package com.sujit.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sujit.payment_service.dto.CheckoutItemDto;
import com.sujit.payment_service.entity.PaymentEntity;
import com.sujit.payment_service.enums.PaymentStatus;
import com.sujit.payment_service.exception.PaymentInternalServerException;
import com.sujit.payment_service.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class OrderService {

    @Value("${stripe.base-url}")
    private String baseURL;

    @Value("${stripe.secret-key}")
    private String apiKey;

    public final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    public OrderService(PaymentRepository paymentRepository,
                        ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
    }

    // Create Stripe checkout session for the given cart items.
    public Session createSession(UUID orderId,
                                 List<CheckoutItemDto> checkoutItemDtoList) {
        try {
            log.info("Creating checkout session for orderId: {}", orderId);

            String successURL = baseURL + "payment/success";
            String failedURL = baseURL + "payment/failed";

            Stripe.apiKey = apiKey;

            List<SessionCreateParams.LineItem> sessionItemsList = new ArrayList<>();
            long totalAmount = 0;

            for (CheckoutItemDto item : checkoutItemDtoList) {
                log.info("Processing item: {}", item);

                sessionItemsList.add(createSessionLineItem(item));
                totalAmount += (long) (item.getPrice() * item.getQuantity());
            }

            totalAmount = totalAmount * 100;
            log.info("Total amount (cents): {}", totalAmount);

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCancelUrl(failedURL)
                    .setSuccessUrl(successURL)
                    .addAllLineItem(sessionItemsList)
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putMetadata("orderId", String.valueOf(orderId))
                                    .build()
                    )
                    .build();

            log.info("Creating Stripe session...");
            Session session = Session.create(params);

            log.info("Stripe session created: {}", session.getId());

            // Find existing payment entity (created by processOrderCreated)
            PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new PaymentInternalServerException("Payment record not found for order: " + orderId));

            // Update the payment entity
            payment.setTransactionId(session.getId());
            payment.setStatus(PaymentStatus.PENDING.name());
            payment.setResponsePayload(objectMapper.writeValueAsString(Map.of(
                    "sessionId", session.getId(),
                    "status", PaymentStatus.PENDING
            )));

            log.info("Updating payment entity to PENDING: {}", payment);
            paymentRepository.save(payment);

            return session;

        } catch (Exception e) {
            log.error("Error creating checkout session for orderId {}: {}", orderId, e.getMessage(), e);
            throw new PaymentInternalServerException("Failed to create checkout session", e);
        }
    }

    // Set quantity
    private SessionCreateParams.LineItem createSessionLineItem(CheckoutItemDto checkoutItemDto) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(createPriceData(checkoutItemDto))
                .setQuantity(Long.parseLong(String.valueOf(checkoutItemDto.getQuantity())))
                .build();
    }

    // Set price and product data
    private SessionCreateParams.LineItem.PriceData createPriceData(CheckoutItemDto checkoutItemDto) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd") // Currency for the transaction
                .setUnitAmount(((long) checkoutItemDto.getPrice()) * 100) // Convert dollars to cents
                .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(checkoutItemDto.getProductName()) // Display name for the product
                                .build())
                .build();
    }
}