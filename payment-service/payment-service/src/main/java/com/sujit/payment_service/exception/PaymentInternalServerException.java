package com.sujit.payment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentInternalServerException extends RuntimeException {

    public HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public PaymentInternalServerException(String msg) {
        super(msg);
    }

    public PaymentInternalServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public PaymentInternalServerException(String message, Exception e) {
        super(message, e);
    }

    public PaymentInternalServerException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}
