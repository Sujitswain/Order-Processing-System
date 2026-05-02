package com.sujit.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfGeneratedEvent {

    private UUID orderId;
    private String customerEmail;
    private String pdfUrl;
    private String fileName;
}