package com.sujit.order_service.config;

public class CorrelationIdHolder {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    public static void setCorrelationId(String correlationId) {
        HOLDER.set(correlationId);
    }

    public static String getCorrelationId() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
