package com.sujit.inventory_service.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private Instant timestamp;
    private int status;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(int status, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.timestamp = Instant.now();
        response.status = status;
        response.data = data;
        response.error = null;
        return response;
    }

    public static <T> ApiResponse<T> failure(int status, String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.timestamp = Instant.now();
        response.status = status;
        response.data = null;
        response.error = error;
        return response;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
