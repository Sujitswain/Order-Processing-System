package com.sujit.payment_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class StripeResponse {
    private String sessionId;
    private String sessionUrl;     // https://checkout.stripe.com/pay/{sessionId}

    public StripeResponse(String sessionId, String sessionUrl) {
        this.sessionId = sessionId;
        this.sessionUrl = sessionUrl;
    }
}