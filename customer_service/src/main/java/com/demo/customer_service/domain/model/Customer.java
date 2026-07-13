package com.demo.customer_service.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Customer {

    private final UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private final Instant createdAt;
    private final List<Address> addresses;

    // Constructor for creating a new customer
    public Customer(String firstName, String lastName, String email) {
        this.id = UUID.randomUUID();
        this.firstName = validateNotBlank(firstName, "firstName");
        this.lastName = validateNotBlank(lastName, "lastName");
        this.email = validateEmail(email);
        this.createdAt = Instant.now();
        this.addresses = new ArrayList<>();
    }

    // Constructor for reconstituting from persistence.DB se wapas laane ke liye
    public Customer(UUID id, String firstName, String lastName, String email,
                    Instant createdAt, List<Address> addresses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = createdAt;
        this.addresses = new ArrayList<>(addresses);
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Address> getAddresses() { return Collections.unmodifiableList(addresses); }

    // --- Domain validation ---

    private String validateNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private String validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
        return email.trim().toLowerCase();
    }
}
