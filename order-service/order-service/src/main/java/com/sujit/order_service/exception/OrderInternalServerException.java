package com.sujit.order_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrderInternalServerException extends RuntimeException {

    public HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public OrderInternalServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public OrderInternalServerException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}