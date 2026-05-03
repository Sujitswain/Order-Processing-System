package com.sujit.pdf_service.client;

import com.sujit.pdf_service.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Slf4j
@Component
public class OrderServiceClient {

    @Value("${api.order.service.base-url}")
    private String baseUrl;

    @Value("${api.order.service.get-orders}")
    private String getOrderUrl;

    private final RestClient restClient;

    public OrderServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public OrderResponse getOrder(UUID orderId) {
        try {
            log.info("Fetching order details for order: {}", orderId);
            return restClient.get()
                    .uri(baseUrl + getOrderUrl, orderId)
                    .retrieve()
                    .body(OrderResponse.class);
        } catch (RestClientException ex) {
            log.error("Unable to fetch order details from Order Service for order: {}", orderId, ex);
            throw new RuntimeException("Unable to fetch order details from Order Service", ex);
        }
    }
}