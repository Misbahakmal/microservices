package com.demo.order_service.domain.model;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event representing an order status change.
 * Schema as for nitification event
 */
public class OrderEvent {

    private final UUID eventId;
    private final UUID orderId;
    private final Instant occurredAt;
    private final OrderStatus status;

    public OrderEvent(UUID orderId, OrderStatus status) {
        this.eventId = UUID.randomUUID();
        this.orderId = orderId;
        this.occurredAt = Instant.now();
        this.status = status;
    }

    public UUID getEventId() { return eventId; }
    public UUID getOrderId() { return orderId; }
    public Instant getOccurredAt() { return occurredAt; }
    public OrderStatus getStatus() { return status; }
}
