package com.demo.customer_service.domain;

import org.junit.jupiter.api.Test;

import com.demo.customer_service.domain.model.Address;
import com.demo.customer_service.domain.model.Customer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// This class needs NO Mockito, NO Spring context, NO @ExtendWith at all.
// It's the cheapest, fastest kind of test there is - just plain object
// construction and assertions - which is exactly why domain validation
// belongs in the domain layer instead of scattered across controllers.
class CustomerTest {

    @Test
    void constructor_createsValidCustomer_trimmedAndLowercasedEmail() {
        Customer customer = new Customer("  John  ", "Doe", "John.Doe@EXAMPLE.com");

        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getAddresses()).isEmpty();
    }

    @Test
    void constructor_throws_whenFirstNameIsBlank() {
        assertThatThrownBy(() -> new Customer("  ", "Doe", "john@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void constructor_throws_whenEmailHasNoAtSymbol() {
        assertThatThrownBy(() -> new Customer("John", "Doe", "not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void addAddress_appendsToImmutableView() {
        Customer customer = new Customer("John", "Doe", "john@example.com");
        Address address = new Address(customer.getId(), "Street 1", "Berlin", "10115", "DE");

        customer.addAddress(address);

        assertThat(customer.getAddresses()).hasSize(1);
        // getAddresses() returns Collections.unmodifiableList - confirm callers
        // genuinely can't mutate internal state through the getter.
        assertThatThrownBy(() -> customer.getAddresses().add(address))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reconstitutionConstructor_preservesGivenIdAndCreatedAt() {
        UUID fixedId = UUID.randomUUID();
        Instant fixedCreatedAt = Instant.parse("2026-01-01T00:00:00Z");

        Customer customer = new Customer(fixedId, "John", "Doe", "john@example.com",
                fixedCreatedAt, List.of());

        assertThat(customer.getId()).isEqualTo(fixedId);
        assertThat(customer.getCreatedAt()).isEqualTo(fixedCreatedAt);
    }
}