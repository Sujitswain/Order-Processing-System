package com.sujit.notification_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotificationBadRequestException extends RuntimeException {

    public HttpStatus status = HttpStatus.BAD_REQUEST;

    public NotificationBadRequestException(String message) {
        super(message);
    }

    public NotificationBadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public NotificationBadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public NotificationBadRequestException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    
}
