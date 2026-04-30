package com.sujit.payment_service.dto.checkout;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StripeResponse {
    private String sessionId;   // https://checkout.stripe.com/pay/{sessionId}

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public StripeResponse(String sessionId) {
        this.sessionId = sessionId;
    }
}