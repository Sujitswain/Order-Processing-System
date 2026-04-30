package com.sujit.payment_service.service;

import com.sujit.payment_service.dto.checkout.CheckoutItemDto;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Stripe checkout session creation.
 * This service creates hosted checkout sessions for e-commerce payments.
 */
@Service
@Transactional
public class OrderService {

    @Value("${stripe.base-url}")
    private String baseURL;

    @Value("${stripe.secret-key}")
    private String apiKey;

    // Create Stripe checkout session for the given cart items.
    public Session createSession(List<CheckoutItemDto> checkoutItemDtoList) throws StripeException {
        // Define URLs for post-payment redirects
        String successURL = baseURL + "payment/success";
        String failedURL = baseURL + "payment/failed";

        // Set Stripe API key for this session (configured globally in StripeConfig)
        Stripe.apiKey = apiKey;

        // Convert cart items to Stripe line items
        List<SessionCreateParams.LineItem> sessionItemsList = new ArrayList<>();
        for (CheckoutItemDto checkoutItemDto : checkoutItemDtoList) {
            sessionItemsList.add(createSessionLineItem(checkoutItemDto));
        }

        // Build session parameters for a payment checkout
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCancelUrl(failedURL)              // Where to redirect on payment cancellation
                .addAllLineItem(sessionItemsList)     // Add all cart items
                .setSuccessUrl(successURL)            // Where to redirect on successful payment
                .build();

        // Create and return the Stripe checkout session
        return Session.create(params);
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