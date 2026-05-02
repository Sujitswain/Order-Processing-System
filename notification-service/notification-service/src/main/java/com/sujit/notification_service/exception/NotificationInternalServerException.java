package com.sujit.notification_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotificationInternalServerException extends RuntimeException {

    public HttpStatus status = HttpStatus.BAD_REQUEST;

    public NotificationInternalServerException(String message) {
        super(message);
    }

    public NotificationInternalServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public NotificationInternalServerException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public NotificationInternalServerException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public NotificationInternalServerException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    
}
