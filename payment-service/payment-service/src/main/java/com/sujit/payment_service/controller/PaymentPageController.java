package com.sujit.payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentPageController {

    // FIXME: TO be replaced by FE React
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess() {
        return ResponseEntity.ok("Payment Successful");
    }

    // FIXME: TO be replaced by FE React
    @GetMapping("/failed")
    public ResponseEntity<String> paymentFailed() {
        return ResponseEntity.ok("Payment Failed");
    }
}