package com.example.demo.exception;

/**
 * Thrown when a requested entity (Order, Product, Customer, Variant, etc.)
 * does not exist in the database. Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Object id) {
        super(resourceType + " with id " + id + " not found");
    }
}
