package com.demo.customer_service.domain;

import org.junit.jupiter.api.Test;

import com.demo.customer_service.domain.model.Address;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Test
    void constructor_createsValidAddress_uppercasesCountry() {
        UUID customerId = UUID.randomUUID();

        Address address = new Address(customerId, "  Carnotstr. 4  ", "Berlin", "10587", "de");

        assertThat(address.getStreet()).isEqualTo("Carnotstr. 4");
        assertThat(address.getCountry()).isEqualTo("DE");
        assertThat(address.getCustomerId()).isEqualTo(customerId);
        assertThat(address.getId()).isNotNull();
    }

    @Test
    void constructor_throws_whenStreetIsBlank() {
        assertThatThrownBy(() -> new Address(UUID.randomUUID(), "  ", "Berlin", "10587", "DE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("street");
    }

    @Test
    void constructor_throws_whenCountryIsNotTwoLetters() {
        assertThatThrownBy(() -> new Address(UUID.randomUUID(), "Street 1", "Berlin", "10587", "Germany"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-letter");
    }

    @Test
    void constructor_throws_whenCountryIsNull() {
        assertThatThrownBy(() -> new Address(UUID.randomUUID(), "Street 1", "Berlin", "10587", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstitutionConstructor_preservesGivenIdWithoutRevalidation() {
        UUID fixedId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // Note: the reconstitution constructor deliberately skips validation
        // (it trusts data already persisted in the DB). This test just
        // documents that behavior - it does NOT mean invalid data should
        // ever reach this constructor in practice.
        Address address = new Address(fixedId, customerId, "Street 1", "Berlin", "10115", "DE");

        assertThat(address.getId()).isEqualTo(fixedId);
    }
}