package com.demo.order_service.infrastructure.adapter.out.messaging;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.demo.order_service.application.port.out.OrderEventPublisherPort;
import com.demo.order_service.domain.model.OrderEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter that implements the outgoing event publisher port.
 * Uses a transactional KafkaTemplate so the Kafka send is part of the same
 * transaction as the DB write — if the DB rolls back, the event is not sent.
 */
@Component
public class KafkaOrderEventPublisher implements OrderEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaOrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                    ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderEvent event, String correlationId) {
        try {
            String payload = objectMapper.writeValueAsString(new OrderEventDto(
                    event.getEventId().toString(),
                    event.getOrderId().toString(),
                    event.getOccurredAt().toString(),
                    event.getStatus().name()
            ));

            var message = MessageBuilder.withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    // Partition key = orderId → same order events always go to same partition
                    .setHeader(KafkaHeaders.KEY, event.getOrderId().toString())
                    .setHeader("correlationId", correlationId)
                    .setHeader("eventId", event.getEventId().toString())
                    .build();

            kafkaTemplate.send(message);
            log.info("Published order event: orderId={} status={} eventId={}",
                    event.getOrderId(), event.getStatus(), event.getEventId());

        } catch (Exception e) {
            log.error("Failed to publish order event for orderId={}", event.getOrderId(), e);
            // Re-throw so the transaction rolls back if still active
            throw new RuntimeException("Failed to publish order event", e);
        }
    }

    // Internal DTO matching the Kafka schema defined in the spec
    record OrderEventDto(String eventId, String orderId, String occurredAt, String status) {}
}
