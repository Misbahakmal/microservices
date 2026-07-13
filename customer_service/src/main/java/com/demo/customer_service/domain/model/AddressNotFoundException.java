package com.demo.customer_service.domain.model;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(UUID customerId, UUID addressId) {
        super("Address " + addressId + " not found for customer " + customerId);
    }
}
