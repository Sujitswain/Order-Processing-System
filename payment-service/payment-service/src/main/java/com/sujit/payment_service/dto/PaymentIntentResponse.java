package com.sujit.payment_service.dto;

import lombok.*;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponse {

    private String paymentIntentId;
    private String clientSecret;
    private String status;

}