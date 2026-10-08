package com.restaurant.billing_service.presentation.exception;

public class OrderServiceUnavailableException extends RuntimeException {
    public OrderServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
