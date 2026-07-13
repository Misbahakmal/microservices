package com.demo.customer_service.domain.model;

public class CustomerEmailAlreadyExistsException extends RuntimeException {
    public CustomerEmailAlreadyExistsException(String email) {
        super("A customer with email '" + email + "' already exists");
    }
}
