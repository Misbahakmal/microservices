package com.demo.customer_service.domain.model;

import java.util.UUID;
public class Address {

    private final UUID id;
    private final UUID customerId;
    private final String street;
    private final String city;
    private final String postalCode;
    private final String country;

    public Address(UUID customerId, String street, String city,
                   String postalCode, String country) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.street = validateNotBlank(street, "street");
        this.city = validateNotBlank(city, "city");
        this.postalCode = validateNotBlank(postalCode, "postalCode");
        this.country = validateCountry(country);
    }

    // Reconstitution constructor
    public Address(UUID id, UUID customerId, String street, String city,
                   String postalCode, String country) {
        this.id = id;
        this.customerId = customerId;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }

    private String validateNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private String validateCountry(String country) {
        if (country == null || country.length() != 2) {
            throw new IllegalArgumentException("country must be a 2-letter ISO code");
        }
        return country.toUpperCase();
    }
}
