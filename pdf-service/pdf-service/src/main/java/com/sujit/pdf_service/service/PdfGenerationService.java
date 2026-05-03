package com.sujit.pdf_service.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sujit.pdf_service.client.OrderServiceClient;
import com.sujit.pdf_service.dto.OrderItemResponse;
import com.sujit.pdf_service.dto.OrderResponse;
import com.sujit.pdf_service.event.OrderSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfGenerationService {

    private final StorageService storageService;
    private final OrderServiceClient orderServiceClient;

    public PdfGenerationService(StorageService storageService, OrderServiceClient orderServiceClient) {
        this.storageService = storageService;
        this.orderServiceClient = orderServiceClient;
    }

    public String generateInvoicePdf(OrderSuccessEvent event) throws IOException {
        // Fetch complete order details from order service
        OrderResponse order = orderServiceClient.getOrder(event.getOrderId());

        String fileName = "invoice-" + order.getOrderId() + ".pdf";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            Paragraph title = new Paragraph("ORDER INVOICE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold();
            document.add(title);

            // Order details
            document.add(new Paragraph("Order ID: " + order.getOrderId()));
            document.add(new Paragraph("Customer ID: " + order.getCustomerId()));
            document.add(new Paragraph("Date: " + order.getCreatedAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            // Items table
            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table headers
            table.addHeaderCell("Product");
            table.addHeaderCell("Quantity");
            table.addHeaderCell("Price");
            table.addHeaderCell("Total");

            // Table rows
            for (OrderItemResponse item : order.getItems()) {
                table.addCell("Product " + item.getProductId());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("$" + item.getPrice());
                table.addCell("$" + item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Total
            Paragraph total = new Paragraph("Total Amount: $" + order.getTotalAmount())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(total);

            // Footer
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Thank you for your business!")
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

            document.close();

            // Save to storage
            return storageService.savePdf(order.getOrderId(), fileName, baos.toByteArray());
        }
    }
}