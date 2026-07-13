package com.demo.order_service.domain.model;

import java.util.UUID;

/**
 * CustomerAddress
 */
public record CustomerAddress(
        UUID customerId,
        UUID addressId,
        String street,
        String city,
        String postalCode,
        String country
) {}
