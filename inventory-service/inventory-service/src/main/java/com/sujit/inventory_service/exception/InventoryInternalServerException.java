package com.sujit.inventory_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InventoryInternalServerException extends RuntimeException {

    public HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public InventoryInternalServerException(String message) {
        super(message);
    }

    public InventoryInternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryInternalServerException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public InventoryInternalServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public InventoryInternalServerException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public InventoryInternalServerException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    
}
