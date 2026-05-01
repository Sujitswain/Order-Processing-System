package com.sujit.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sujit.payment_service.config.CorrelationIdFilter;
import com.sujit.payment_service.config.CorrelationIdHolder;
import com.sujit.payment_service.event.OrderCreatedEvent;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public PaymentEventListener(ObjectMapper objectMapper, PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-created", groupId = "payment-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCreated(ConsumerRecord<String, Object> record) throws Exception {
        setCorrelationId(record);
        String json = record.value() != null ? record.value().toString() : "";
        OrderCreatedEvent event = objectMapper.readValue(json, OrderCreatedEvent.class);
        paymentService.processOrderCreated(event);
    }

    private void setCorrelationId(ConsumerRecord<String, Object> record) {
        Header header = record.headers().lastHeader(CorrelationIdFilter.CORRELATION_HEADER);
        if (header != null) {
            CorrelationIdHolder.setCorrelationId(new String(header.value(), StandardCharsets.UTF_8));
        }
    }
}
