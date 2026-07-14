package com.example.demo.exception;

/**
 * Thrown when Razorpay signature verification fails or payment processing errors out.
 * Mapped to HTTP 403 by GlobalExceptionHandler.
 */
public class PaymentVerificationException extends RuntimeException {
    public PaymentVerificationException(String message) {
        super(message);
    }
}
