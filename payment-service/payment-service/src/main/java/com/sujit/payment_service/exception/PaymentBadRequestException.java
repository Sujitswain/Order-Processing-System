package com.sujit.payment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentBadRequestException extends RuntimeException {

    public HttpStatus status = HttpStatus.BAD_REQUEST;

    public PaymentBadRequestException(String message) {
        super(message);
    }

    public PaymentBadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public PaymentBadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public PaymentBadRequestException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
