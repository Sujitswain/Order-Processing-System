package com.sujit.inventory_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sujit.inventory_service.config.CorrelationIdFilter;
import com.sujit.inventory_service.config.CorrelationIdHolder;
import java.nio.charset.StandardCharsets;

import com.sujit.inventory_service.event.OrderCancelledEvent;
import com.sujit.inventory_service.event.OrderCreatedEvent;
import com.sujit.inventory_service.event.PaymentSuccessEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;

    public InventoryEventListener(ObjectMapper objectMapper, InventoryService inventoryService) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCreated(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
        inventoryService.handleOrderCreated(event);
    }

    @KafkaListener(topics = "payment-success", groupId = "inventory-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentSuccess(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        PaymentSuccessEvent event = objectMapper.readValue(record.value(), PaymentSuccessEvent.class);
        inventoryService.handlePaymentSuccess(event);
    }

    @KafkaListener(topics = "order-cancelled", groupId = "inventory-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCancelled(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        OrderCancelledEvent event = objectMapper.readValue(record.value(), OrderCancelledEvent.class);
        inventoryService.handleOrderCancelled(event);
    }

    private void setCorrelationId(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(CorrelationIdFilter.CORRELATION_HEADER);
        if (header != null) {
            CorrelationIdHolder.setCorrelationId(new String(header.value(), StandardCharsets.UTF_8));
        }
    }
}
