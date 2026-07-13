package com.demo.order_service.domain.model;


import java.util.UUID;

public class CustomerValidationException extends RuntimeException {
    public CustomerValidationException(UUID customerId, String reason) {
        super("Customer validation failed for ID " + customerId + ": " + reason);
    }
}