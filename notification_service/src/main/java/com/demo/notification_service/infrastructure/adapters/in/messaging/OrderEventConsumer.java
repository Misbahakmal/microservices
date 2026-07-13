package com.demo.notification_service.infrastructure.adapters.in.messaging;

import com.demo.notification_service.application.usecase.NotificationService;
// 💡 THE ENCODING/DECODING FIX: Standard package naming structures ko target karein
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderEventConsumer {

    private final NotificationService notificationService;

    // Manual ObjectMapper parsing ki ab zaroori nahi, framework automatic deserialize karega
    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderEvent(@Payload JsonNode jsonNode) { // 💡 Direct JsonNode payload accept karein
        try {
            System.out.println("====== EVENT PULL HANDSHAKE SUCCESS ======");
            
            // com.fasterxml.jackson.databind.JsonNode mein data extract karne ke liye .asText() ya .textValue() best hai
            UUID eventId = UUID.fromString(jsonNode.get("eventId").asText());
            UUID orderId = UUID.fromString(jsonNode.get("orderId").asText());
            String status = jsonNode.get("status").asText();
            
            // Use Case core processing layer invocation trigger
            notificationService.processEvent(eventId, orderId, status);
            
            System.out.println("Successfully processed event for order: " + orderId);
            System.out.println("=========================================");
            
        } catch (Exception e) {
            System.err.println("Error processing Kafka message processing adapter: " + e.getMessage());
            e.printStackTrace();
        }
    }
}