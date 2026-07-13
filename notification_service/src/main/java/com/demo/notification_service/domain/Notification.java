package com.demo.notification_service.domain;

import java.time.Instant;
import java.util.UUID;

public class Notification {
    private final UUID id;
    private final UUID eventId;
    private final UUID orderId;
    private final String message;
    private final Instant sentAt;

    public Notification(UUID id, UUID eventId, UUID orderId, String message, Instant sentAt) {
        this.id = id;
        this.eventId = eventId;
        this.orderId = orderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Sirf Getters (Immutable domain model)
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getOrderId() { return orderId; }
    public String getMessage() { return message; }
    public Instant getSentAt() { return sentAt; }
}
