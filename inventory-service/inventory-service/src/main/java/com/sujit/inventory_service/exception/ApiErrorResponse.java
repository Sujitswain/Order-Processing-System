package com.sujit.inventory_service.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiErrorResponse<T> {

    private Instant timestamp;
    private int status;
    private T data;
    private String error;

    public static <T> ApiErrorResponse<T> failure(int status, String error) {
        ApiErrorResponse<T> response = new ApiErrorResponse<>();
        response.timestamp = Instant.now();
        response.status = status;
        response.data = null;
        response.error = error;
        return response;
    }
}
