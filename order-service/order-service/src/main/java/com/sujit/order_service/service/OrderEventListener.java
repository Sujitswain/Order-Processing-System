package com.sujit.order_service.service;

import com.sujit.order_service.config.CorrelationIdHolder;
import com.sujit.order_service.config.CorrelationIdFilter;
import java.nio.charset.StandardCharsets;

import com.sujit.order_service.event.OrderCompletedEvent;
import com.sujit.order_service.event.PaymentFailedEvent;
import com.sujit.order_service.event.PaymentSuccessEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public OrderEventListener(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(topics = "order-completed", groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCompleted(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);

        OrderCompletedEvent event = objectMapper.readValue(record.value(), OrderCompletedEvent.class);
        orderService.markOrderCompleted(event.getOrderId());
    }

    @KafkaListener(topics = "payment-success", groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentSuccess(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);

        PaymentSuccessEvent event = objectMapper.readValue(record.value(), PaymentSuccessEvent.class);
        orderService.markOrderCompleted(event.getOrderId());
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentFailed(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);

        PaymentFailedEvent event = objectMapper.readValue(record.value(), PaymentFailedEvent.class);
        orderService.markOrderPaymentFailed(event.getOrderId());
    }

    private void setCorrelationId(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(CorrelationIdFilter.CORRELATION_HEADER);
        if (header != null) {
            CorrelationIdHolder.setCorrelationId(new String(header.value(), StandardCharsets.UTF_8));
        }
    }
}
