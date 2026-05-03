package com.sujit.pdf_service.controller;

import com.sujit.pdf_service.service.StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final StorageService storageService;

    public InvoiceController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<byte[]> getInvoiceByOrderId(@PathVariable UUID orderId) {
        try {
            byte[] pdfBytes = storageService.getPdfByOrderId(orderId);
            String fileName = "invoice-" + orderId + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
