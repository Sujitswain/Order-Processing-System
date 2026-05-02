package com.sujit.notification_service.service;

import com.sujit.notification_service.config.CorrelationIdFilter;
import com.sujit.notification_service.config.CorrelationIdHolder;
import java.nio.charset.StandardCharsets;

import com.sujit.notification_service.event.OrderCompletedEvent;
import com.sujit.notification_service.event.PaymentFailedEvent;
import com.sujit.notification_service.event.PaymentSuccessEvent;
import com.sujit.notification_service.event.PdfGeneratedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificationEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationEventListener(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "payment-success", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentSuccess(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        PaymentSuccessEvent event = objectMapper.readValue(record.value(), PaymentSuccessEvent.class);
        notificationService.handlePaymentSuccess(event);
    }

    @KafkaListener(topics = "payment-failed", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentFailed(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        PaymentFailedEvent event = objectMapper.readValue(record.value(), PaymentFailedEvent.class);
        notificationService.handlePaymentFailed(event);
    }

    @KafkaListener(topics = "order-completed", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCompleted(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        OrderCompletedEvent event = objectMapper.readValue(record.value(), OrderCompletedEvent.class);
        notificationService.handleOrderCompleted(event);
    }

    @KafkaListener(topics = "pdf-generated", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void onPdfGenerated(ConsumerRecord<String, String> record) throws Exception {
        setCorrelationId(record);
        PdfGeneratedEvent event = objectMapper.readValue(record.value(), PdfGeneratedEvent.class);
        notificationService.handlePdfGenerated(event);
    }

    private void setCorrelationId(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(CorrelationIdFilter.CORRELATION_HEADER);
        if (header != null) {
            CorrelationIdHolder.setCorrelationId(new String(header.value(), StandardCharsets.UTF_8));
        }
    }
}
