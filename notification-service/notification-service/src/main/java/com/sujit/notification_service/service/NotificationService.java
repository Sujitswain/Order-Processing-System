package com.sujit.notification_service.service;

import com.sujit.notification_service.client.OrderServiceClient;
import com.sujit.notification_service.dto.NotificationResponse;
import com.sujit.notification_service.dto.OrderResponse;
import com.sujit.notification_service.entity.NotificationLog;
import com.sujit.notification_service.event.OrderCompletedEvent;
import com.sujit.notification_service.event.PaymentFailedEvent;
import com.sujit.notification_service.event.PaymentSuccessEvent;
import com.sujit.notification_service.event.PdfGeneratedEvent;
import com.sujit.notification_service.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;
    private final OrderServiceClient orderServiceClient;

    public NotificationService(NotificationLogRepository notificationLogRepository,
                              EmailService emailService,
                              OrderServiceClient orderServiceClient) {
        this.notificationLogRepository = notificationLogRepository;
        this.emailService = emailService;
        this.orderServiceClient = orderServiceClient;
    }

    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        try {
            OrderResponse order = orderServiceClient.getOrder(event.getOrderId());
            emailService.sendPaymentSuccessEmail(order.getCustomerEmail(), order);
            NotificationLog log = createLog(event.getOrderId(), order.getCustomerEmail(), "PAYMENT_SUCCESS", "SENT");
            notificationLogRepository.save(log);
        } catch (Exception e) {
            NotificationLog log = createLog(event.getOrderId(), "unknown", "PAYMENT_SUCCESS", "FAILED");
            notificationLogRepository.save(log);
        }
    }

    public void handlePaymentFailed(PaymentFailedEvent event) {
        try {
            OrderResponse order = orderServiceClient.getOrder(event.getOrderId());
            emailService.sendPaymentFailedEmail(order.getCustomerEmail(), order);
            NotificationLog log = createLog(event.getOrderId(), order.getCustomerEmail(), "PAYMENT_FAILED", "SENT");
            notificationLogRepository.save(log);
        } catch (Exception e) {
            NotificationLog log = createLog(event.getOrderId(), "unknown", "PAYMENT_FAILED", "FAILED");
            notificationLogRepository.save(log);
        }
    }

    public void handleOrderCompleted(OrderCompletedEvent event) {
        try {
            OrderResponse order = orderServiceClient.getOrder(event.getOrderId());
            emailService.sendOrderCompletedEmail(order.getCustomerEmail(), order);
            NotificationLog log = createLog(event.getOrderId(), order.getCustomerEmail(), "ORDER_COMPLETED", "SENT");
            notificationLogRepository.save(log);
        } catch (Exception e) {
            NotificationLog log = createLog(event.getOrderId(), "unknown", "ORDER_COMPLETED", "FAILED");
            notificationLogRepository.save(log);
        }
    }

    public void handlePdfGenerated(PdfGeneratedEvent event) {
        try {
            emailService.sendInvoiceEmail(event.getCustomerEmail(), event.getOrderId(), event.getPdfUrl());
            NotificationLog log = createLog(event.getOrderId(), event.getCustomerEmail(), "INVOICE_READY", "SENT");
            notificationLogRepository.save(log);
        } catch (Exception e) {
            NotificationLog log = createLog(event.getOrderId(), event.getCustomerEmail(), "INVOICE_READY", "FAILED");
            notificationLogRepository.save(log);
        }
    }

    public List<NotificationResponse> findByOrderId(UUID orderId) {
        return notificationLogRepository.findByOrderId(orderId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private NotificationResponse toDto(NotificationLog entity) {
        NotificationResponse response = new NotificationResponse();
        response.setOrderId(entity.getOrderId());
        response.setRecipient(entity.getRecipient());
        response.setType(entity.getType());
        response.setStatus(entity.getStatus());
        response.setSentAt(entity.getSentAt());
        return response;
    }

    private NotificationLog createLog(UUID orderId, String recipient, String type, String status) {
        NotificationLog log = new NotificationLog();
        log.setOrderId(orderId);
        log.setRecipient(recipient);
        log.setType(type);
        log.setStatus(status);
        log.setSentAt(Instant.now());
        return log;
    }
}
