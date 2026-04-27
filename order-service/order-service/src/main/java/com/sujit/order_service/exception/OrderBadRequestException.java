package com.sujit.order_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrderBadRequestException extends RuntimeException {

    public HttpStatus status = HttpStatus.BAD_REQUEST;

    public OrderBadRequestException(String message) {
        super(message);
    }

    public OrderBadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public OrderBadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public OrderBadRequestException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}
