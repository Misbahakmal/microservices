package com.demo.customer_service.domain.model;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID id) {
        super("Customer not found: " + id);
    }
}
