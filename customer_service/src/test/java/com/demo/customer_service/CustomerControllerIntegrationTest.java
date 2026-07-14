package com.demo.customer_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// No more Testcontainers here. This test assumes the real docker-compose
// infrastructure (postgres + eureka-server) is ALREADY running before
// Maven starts - either started manually for local runs, or started by
// the CI workflow before the build/test step.
//
// Run locally with:
//   docker compose up -d postgres eureka-server
//   cd customer_service && mvn test -Dspring.profiles.active=ci
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCustomer_thenGetById_returnsSameCustomer() throws Exception {
        var createRequest = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                // unique email per run so repeated local test runs don't
                // collide with leftover data from a previous run
                "email", "john.doe." + UUID.randomUUID() + "@example.com"
        );

        String response = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andReturn().getResponse().getContentAsString();

        String customerId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getAddressById_returns200_whenAddressBelongsToCustomer() throws Exception {
        String customerResponse = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Jane", "lastName", "Smith",
                                "email", "jane." + UUID.randomUUID() + "@example.com"))))
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

        mockMvc.perform(get("/customers/{id}/addresses/{addressId}", customerId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Carnotstr. 4"))
                .andExpect(jsonPath("$.customerId").value(customerId));
    }

    @Test
    void getAddressById_returns404_whenAddressBelongsToDifferentCustomer() throws Exception {
        String customerAResponse = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Alice", "lastName", "A",
                                "email", "alice." + UUID.randomUUID() + "@example.com"))))
                .andReturn().getResponse().getContentAsString();
        String customerAId = objectMapper.readTree(customerAResponse).get("id").asText();

        String addressResponse = mockMvc.perform(post("/customers/{id}/addresses", customerAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "street", "Alice Str. 1", "city", "Berlin",
                                "postalCode", "10115", "country", "DE"))))
                .andReturn().getResponse().getContentAsString();
        String addressId = objectMapper.readTree(addressResponse).get("id").asText();

        String customerBResponse = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Bob", "lastName", "B",
                                "email", "bob." + UUID.randomUUID() + "@example.com"))))
                .andReturn().getResponse().getContentAsString();
        String customerBId = objectMapper.readTree(customerBResponse).get("id").asText();

        mockMvc.perform(get("/customers/{id}/addresses/{addressId}", customerBId, addressId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerById_returns404_whenCustomerDoesNotExist() throws Exception {
        mockMvc.perform(get("/customers/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}