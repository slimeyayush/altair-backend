package com.example.demo.exception;

/**
 * Thrown when a business rule is violated — e.g. insufficient stock,
 * cancelling an already-cancelled order, duplicate admin username.
 * Mapped to HTTP 400 by GlobalExceptionHandler.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
