package com.demo.customer_service;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest boots the FULL application context - real controllers,
// real use cases, real JPA - only the database is swapped for a throwaway
// container instead of your actual "microservices_db". This is the test
// that actually proves your REST layer, JPA mapping, and validation logic
// work together end-to-end.
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerControllerIntegrationTest {

    // NOTE: for test simplicity this uses the default "public" schema
    // instead of "customer_schema" + a dedicated app user, unlike the real
    // docker-compose setup. That schema/user separation is a production
    // concern (least-privilege DB access) - it doesn't need to be
    // re-created for a throwaway test container that's destroyed after
    // every run.
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

   @DynamicPropertySource
static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCustomer_thenGetById_returnsSameCustomer() throws Exception {
        var createRequest = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "email", "john.doe@example.com"
        );

        String response = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andReturn().getResponse().getContentAsString();

        String customerId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getAddressById_returns200_whenAddressBelongsToCustomer() throws Exception {
        // Arrange: create a customer, then add an address to them
        String customerResponse = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Jane", "lastName", "Smith", "email", "jane@example.com"))))
                .andReturn().getResponse().getContentAsString();
        String customerId = objectMapper.readTree(customerResponse).get("id").asText();

        String addressResponse = mockMvc.perform(post("/customers/{id}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "street", "Carnotstr. 4", "city", "Berlin",
                                "postalCode", "10587", "country", "DE"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String addressId = objectMapper.readTree(addressResponse).get("id").asText();

        // Act + Assert: this is the exact endpoint built earlier in this project
        mockMvc.perform(get("/customers/{id}/addresses/{addressId}", customerId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Carnotstr. 4"))
                .andExpect(jsonPath("$.customerId").value(customerId));
    }

    @Test
    void getAddressById_returns404_whenAddressBelongsToDifferentCustomer() throws Exception {
        // Customer A with an address
        String customerAResponse = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Alice", "lastName", "A", "email", "alice@example.com"))))
                .andReturn().getResponse().getContentAsString();
        String customerAId = objectMapper.readTree(customerAResponse).get("id").asText();

        String addressResponse = mockMvc.perform(post("/customers/{id}/addresses", customerAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "street", "Alice Str. 1", "city", "Berlin",
                                "postalCode", "10115", "country", "DE"))))
                .andReturn().getResponse().getContentAsString();
        String addressId = objectMapper.readTree(addressResponse).get("id").asText();

        // Customer B, unrelated
        String customerBResponse = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Bob", "lastName", "B", "email", "bob@example.com"))))
                .andReturn().getResponse().getContentAsString();
        String customerBId = objectMapper.readTree(customerBResponse).get("id").asText();

        // This is the exact validation scenario discussed earlier:
        // Customer B trying to access Customer A's address -> must be 404
        mockMvc.perform(get("/customers/{id}/addresses/{addressId}", customerBId, addressId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerById_returns404_whenCustomerDoesNotExist() throws Exception {
        mockMvc.perform(get("/customers/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
