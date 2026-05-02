package com.sujit.notification_service.client;

import com.sujit.notification_service.dto.OrderResponse;
import com.sujit.notification_service.exception.NotificationInternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

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
            return restClient.get()
                    .uri(baseUrl + getOrderUrl, orderId)
                    .retrieve()
                    .body(OrderResponse.class);
        } catch (RestClientException ex) {
            throw new NotificationInternalServerException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to fetch order details from Order Service", ex);
        }
    }
}
