package com.demo.notification_service.infrastructure.adapters.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "notifications", schema = "notification_schema")
public class NotificationEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", unique = true, nullable = false)
    private UUID eventId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    private String message;

    @Column(name = "sent_at")
    private Instant sentAt;

    // Default Constructor (JPA ke liye zaroori hai)
    public NotificationEntity() {}

    // Full Constructor (Adapter ke liye)
    public NotificationEntity(UUID id, UUID eventId, UUID orderId, String message, Instant sentAt) {
        this.id = id;
        this.eventId = eventId;
        this.orderId = orderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Getters aur Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}