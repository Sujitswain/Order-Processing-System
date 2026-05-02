package com.sujit.notification_service.service;

import com.sujit.notification_service.dto.OrderResponse;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@ordersystem.com");

        mailSender.send(message);
    }

    public void sendPaymentSuccessEmail(String customerEmail, OrderResponse order) {
        String subject = "Payment Successful - Order #" + order.getOrderId();
        String body = String.format("""
            Dear Customer,

            Your payment has been successfully processed!

            Order ID: %s
            Payment Amount: $%.2f
            Payment Status: Completed

            Your order is now being prepared for shipment.

            Best regards,
            Order Processing System
            """, order.getOrderId(), order.getTotalAmount());

        sendEmail(customerEmail, subject, body);
    }

    public void sendPaymentFailedEmail(String customerEmail, OrderResponse order) {
        String subject = "Payment Failed - Order #" + order.getOrderId();
        String body = String.format("""
            Dear Customer,

            We were unable to process your payment for Order #%s.

            Order Amount: $%.2f
            Please check your payment method and try again, or contact our support team.

            Best regards,
            Order Processing System
            """, order.getOrderId(), order.getTotalAmount());

        sendEmail(customerEmail, subject, body);
    }

    public void sendOrderCompletedEmail(String customerEmail, OrderResponse order) {
        String subject = "Order Completed - Order #" + order.getOrderId();
        String body = String.format("""
            Dear Customer,

            Your order has been completed and is ready for pickup/delivery!

            Order ID: %s
            Total Amount: $%.2f
            Status: Completed

            Thank you for your business!

            Best regards,
            Order Processing System
            """, order.getOrderId(), order.getTotalAmount());

        sendEmail(customerEmail, subject, body);
    }

    public void sendInvoiceEmail(String customerEmail, UUID orderId, String pdfUrl) {
        String subject = "Your Invoice - Order #" + orderId;
        String body = String.format("""
            Dear Customer,

            Your order has been successfully processed! Please find your invoice attached.

            Order ID: %s

            Download your invoice: %s

            Thank you for shopping with us!

            Best regards,
            Order Processing System
            """, orderId, pdfUrl);

        sendEmail(customerEmail, subject, body);
    }
}
