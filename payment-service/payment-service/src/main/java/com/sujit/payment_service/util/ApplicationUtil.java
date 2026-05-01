package com.sujit.payment_service.util;

import com.sujit.payment_service.dto.PaymentResponse;
import com.sujit.payment_service.entity.PaymentEntity;

public class ApplicationUtil {

    public static PaymentResponse mapToPaymentresponse(PaymentEntity payment) {
        return PaymentResponse.builder()
                .orderId(payment.getOrderId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .responsePayload(payment.getResponsePayload())
                .build();
    }

}
