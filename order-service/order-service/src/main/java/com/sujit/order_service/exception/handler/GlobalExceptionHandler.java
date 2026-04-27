package com.sujit.order_service.exception.handler;

import com.sujit.order_service.exception.ApiErrorResponse;
import com.sujit.order_service.exception.OrderBadRequestException;
import com.sujit.order_service.exception.OrderInternalServerException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse<String>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.failure(HttpStatus.BAD_REQUEST.value(), details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.failure(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(OrderBadRequestException.class)
    public ResponseEntity<ApiErrorResponse<String>> handleOrderException(OrderBadRequestException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.failure(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler({Exception.class, OrderInternalServerException.class})
    public ResponseEntity<ApiErrorResponse<String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred: " + ex.getMessage()));
    }
}
