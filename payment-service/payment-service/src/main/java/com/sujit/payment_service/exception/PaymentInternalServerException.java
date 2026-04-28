package com.sujit.payment_service.exception;

import org.springframework.http.HttpStatus;

public class PaymentInternalServerException extends RuntimeException {

    public HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public PaymentInternalServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public PaymentInternalServerException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
