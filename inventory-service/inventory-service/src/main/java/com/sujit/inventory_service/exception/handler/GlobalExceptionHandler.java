package com.sujit.inventory_service.exception.handler;

import com.sujit.inventory_service.exception.ApiErrorResponse;
import com.sujit.inventory_service.exception.InventoryBadRequestException;
import com.sujit.inventory_service.exception.InventoryInternalServerException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiErrorResponse.failure(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.failure(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(InventoryBadRequestException.class)
    public ResponseEntity<ApiErrorResponse<String>> handlePaymentException(InventoryBadRequestException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.failure(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler({Exception.class, InventoryInternalServerException.class})
    public ResponseEntity<ApiErrorResponse<String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred: " + ex.getMessage()));
    }
}
