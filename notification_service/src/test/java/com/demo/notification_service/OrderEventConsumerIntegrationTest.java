package com.demo.notification_service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.demo.notification_service.infrastructure.adapters.out.persistence.JpaNotificationRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// This test produces REAL events onto the real "order-events" Kafka topic
// (same one order-service publishes to) and verifies the @KafkaListener
// actually consumes them and persists a Notification row - true end-to-end
// proof, not just a mocked unit test.
//
// Run locally with:
//   docker compose up -d postgres kafka
//   cd notification_service && mvn test -Dspring.profiles.active=ci
@SpringBootTest
@ActiveProfiles("ci")
class OrderEventConsumerIntegrationTest {

    @Autowired
    private JpaNotificationRepository notificationRepository;

    private KafkaProducer<String, String> producer;

    @BeforeEach
    void setUpProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer = new KafkaProducer<>(props);
    }

    @AfterEach
    void tearDownProducer() {
        producer.close();
    }

    @Test
    void consumesOrderEvent_andSavesNotification() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String eventJson = buildEventJson(eventId, orderId, "CREATED");

        producer.send(new ProducerRecord<>("order-events", orderId.toString(), eventJson)).get();

        boolean saved = waitUntil(() -> notificationRepository.existsByEventId(eventId), Duration.ofSeconds(15));

        assertThat(saved)
                .as("Expected a Notification row for eventId=%s within 15s", eventId)
                .isTrue();
    }

    @Test
    void duplicateEvent_doesNotCreateDuplicateNotification() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String eventJson = buildEventJson(eventId, orderId, "SHIPPED");

        long countBefore = notificationRepository.count();

        // Send the SAME event twice - simulates Kafka's at-least-once
        // delivery guarantee, where duplicate delivery is expected, not
        // an edge case.
        producer.send(new ProducerRecord<>("order-events", orderId.toString(), eventJson)).get();
        producer.send(new ProducerRecord<>("order-events", orderId.toString(), eventJson)).get();

        // Wait for at least the first one to be processed
        waitUntil(() -> notificationRepository.existsByEventId(eventId), Duration.ofSeconds(15));

        // Give the (potential) duplicate a little extra time to also be
        // processed, if the idempotency check were broken.
        Thread.sleep(3000);

        long countAfter = notificationRepository.count();

        assertThat(countAfter - countBefore)
                .as("Expected exactly ONE new notification despite the event being sent twice")
                .isEqualTo(1);
    }

    private String buildEventJson(UUID eventId, UUID orderId, String status) {
        return String.format(
                "{\"eventId\":\"%s\",\"orderId\":\"%s\",\"occurredAt\":\"%s\",\"status\":\"%s\"}",
                eventId, orderId, Instant.now(), status);
    }

    private boolean waitUntil(java.util.function.Supplier<Boolean> condition, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (Boolean.TRUE.equals(condition.get())) {
                return true;
            }
            Thread.sleep(500);
        }
        return Boolean.TRUE.equals(condition.get());
    }
}
