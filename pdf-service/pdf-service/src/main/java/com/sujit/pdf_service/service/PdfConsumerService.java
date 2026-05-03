package com.sujit.pdf_service.service;

import com.sujit.pdf_service.event.OrderSuccessEvent;
import com.sujit.pdf_service.event.PdfGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfConsumerService {

    private final PdfGenerationService pdfGenerationService;
    private final KafkaTemplate<String, PdfGeneratedEvent> kafkaTemplate;

    @KafkaListener(topics = "order-success", groupId = "pdf-service-group")
    public void handleOrderSuccess(OrderSuccessEvent event) {
        log.info("Received order success event for order: {}", event.getOrderId());

        try {
            // Generate PDF invoice
            String pdfUrl = pdfGenerationService.generateInvoicePdf(event);
            String fileName = "invoice-" + event.getOrderId() + ".pdf";

            // Create PDF generated event
            PdfGeneratedEvent pdfEvent = PdfGeneratedEvent.builder()
                    .orderId(event.getOrderId())
                    .customerEmail(event.getCustomerEmail())
                    .pdfUrl(pdfUrl)
                    .fileName(fileName)
                    .build();

            // Send event to notification service
            kafkaTemplate.send("pdf-generated", pdfEvent);
            log.info("PDF generated and event sent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to generate PDF for order: {}", event.getOrderId(), e);
            // TODO: Could send to dead letter queue or retry topic
        }
    }
}