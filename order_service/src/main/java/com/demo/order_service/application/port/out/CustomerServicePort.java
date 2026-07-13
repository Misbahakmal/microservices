package com.demo.order_service.application.port.out;

import java.util.UUID;

import com.demo.order_service.domain.model.CustomerAddress;

public interface CustomerServicePort {
 
    /**
     * Validates the customer and address exist.
     * Throws CustomerValidationException if not found.
     */
   // void validateCustomerAndAddress(UUID customerId, UUID addressId);
    CustomerAddress fetchValidatedCustomerAddress(UUID customerId, UUID addressId);
}

