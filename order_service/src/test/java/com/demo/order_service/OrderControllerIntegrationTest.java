package com.demo.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
 
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
 
import com.github.tomakehurst.wiremock.client.WireMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
// This test assumes Postgres and Kafka are ALREADY running via docker-compose
// before Maven starts (same pattern as CustomerControllerIntegrationTest).
// Customer Service itself is NOT started - its response is stubbed with
// WireMock, so this test - and therefore this service's CI pipeline -
// never depends on customer-service's code being correct or even present.
//
// Run locally with:
//   docker compose up -d postgres kafka
//   cd order_service && mvn test -Dspring.profiles.active=ci
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class OrderControllerIntegrationTest {
 
    private static WireMockServer wireMockServer;
 
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private ObjectMapper objectMapper;
 
    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0); // 0 = pick a free random port
        wireMockServer.start();
    }
 
    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }
 
    // Points the "customer-service" Feign client directly at WireMock's URL,
    // bypassing Eureka-based service discovery entirely for this test.
    @DynamicPropertySource
    static void overrideFeignUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.customer-service.url",
                () -> "http://localhost:" + wireMockServer.port());
    }
 
    @BeforeEach
    void resetStubs() {
        wireMockServer.resetAll();
    }
 
    // ─── createOrder ────────────────────────────────────────────────────
 
    @Test
    void createOrder_returns201_whenCustomerAndAddressAreValid() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        stubValidCustomerAddress(customerId, addressId);
 
        var request = Map.of(
                "customerId", customerId.toString(),
                "addressId", addressId.toString(),
                "items", List.of(Map.of(
                        "productId", UUID.randomUUID().toString(),
                        "productName", "Widget",
                        "quantity", 2,
                        "unitPrice", 9.99))
        );
 
        // idempotencyKey is a required HEADER (UUID), not a body field -
        // matches OrdersApi.createOrder(UUID idempotencyKey, CreateOrderRequest)
        mockMvc.perform(post("/orders")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
 
    @Test
    void createOrder_returnsClientError_whenCustomerAddressInvalid() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        // customer-service responds 404 -> Feign's error decoder should turn
        // this into a CustomerValidationException in Order Service
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/customers/.*/addresses/.*"))
                .willReturn(WireMock.aResponse().withStatus(404)));
 
        var request = Map.of(
                "customerId", customerId.toString(),
                "addressId", addressId.toString(),
                "items", List.of(Map.of(
                        "productId", UUID.randomUUID().toString(),
                        "productName", "Widget",
                        "quantity", 1,
                        "unitPrice", 5.00))
        );
 
        mockMvc.perform(post("/orders")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
 
    @Test
    void createOrder_publishesEventToKafka() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        stubValidCustomerAddress(customerId, addressId);
 
        var request = Map.of(
                "customerId", customerId.toString(),
                "addressId", addressId.toString(),
                "items", List.of(Map.of(
                        "productId", UUID.randomUUID().toString(),
                        "productName", "Widget",
                        "quantity", 1,
                        "unitPrice", 5.00))
        );
 
        String response = mockMvc.perform(post("/orders")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String orderId = objectMapper.readTree(response).get("id").asText();
 
        // Real end-to-end proof: actually consume from the real Kafka topic
        // and confirm the event landed there, instead of only trusting the
        // HTTP response.
        try (KafkaConsumer<String, String> consumer = createTestConsumer()) {
            consumer.subscribe(List.of("order-events"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
 
            boolean found = false;
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains(orderId) && record.value().contains("CREATED")) {
                    found = true;
                    break;
                }
            }
            assertThat(found)
                    .as("Expected an OrderCreated event for orderId=%s on order-events topic", orderId)
                    .isTrue();
        }
    }
 
    // ─── getOrderById ───────────────────────────────────────────────────
 
    @Test
    void getOrderById_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/orders/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
 
    // ─── updateStatus ───────────────────────────────────────────────────
 
    @Test
    void updateOrderStatus_shipsOrder_andReturnsUpdatedStatus() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        stubValidCustomerAddress(customerId, addressId);
 
        var createRequest = Map.of(
                "customerId", customerId.toString(),
                "addressId", addressId.toString(),
                "items", List.of(Map.of(
                        "productId", UUID.randomUUID().toString(),
                        "productName", "Widget",
                        "quantity", 1,
                        "unitPrice", 5.00))
        );
 
        String createResponse = mockMvc.perform(post("/orders")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();
        String orderId = objectMapper.readTree(createResponse).get("id").asText();
 
        mockMvc.perform(patch("/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "SHIPPED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
 
    // ─── helpers ────────────────────────────────────────────────────────
 
    private void stubValidCustomerAddress(UUID customerId, UUID addressId) throws Exception {
        var addressResponse = Map.of(
                "id", addressId.toString(),
                "customerId", customerId.toString(),
                "street", "Carnotstr. 4",
                "city", "Berlin",
                "postalCode", "10587",
                "country", "DE"
        );
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/customers/" + customerId + "/addresses/" + addressId))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(addressResponse))));
    }
 
    private KafkaConsumer<String, String> createTestConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props);
    }
}