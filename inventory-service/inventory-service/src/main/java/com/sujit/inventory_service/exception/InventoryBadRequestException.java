package com.sujit.inventory_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InventoryBadRequestException extends RuntimeException {
    
    private HttpStatus status = HttpStatus.BAD_REQUEST;

    public InventoryBadRequestException(String message) {
        super(message);
    }

    public InventoryBadRequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public InventoryBadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public InventoryBadRequestException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
