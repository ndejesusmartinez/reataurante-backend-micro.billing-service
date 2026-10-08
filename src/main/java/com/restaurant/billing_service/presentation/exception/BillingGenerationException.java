package com.restaurant.billing_service.presentation.exception;

public class BillingGenerationException extends RuntimeException {
    public BillingGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
