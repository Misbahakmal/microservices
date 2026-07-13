package com.demo.order_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Order aggregate root.
 * Contains the status state machine — no framework dependencies.
 */
public class Order {

    private final UUID id;
    private final UUID customerId;
    private final UUID addressId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private Instant updatedAt;
    private final String idempotencyKey;

    // Factory method for new orders
    public static Order create(UUID customerId, UUID addressId, List<OrderItem> items,String idempotencyKey) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        return new Order(UUID.randomUUID(), customerId, addressId, OrderStatus.CREATED,
                items, Instant.now(), Instant.now(),idempotencyKey);
    }

    // Reconstitution constructor
    public Order(UUID id, UUID customerId, UUID addressId, OrderStatus status,
                 List<OrderItem> items, Instant createdAt, Instant updatedAt,String idempotencyKey) {
        this.id = id;
        this.customerId = customerId;
        this.addressId = addressId;
        this.status = status;
        this.items = new ArrayList<>(items);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.idempotencyKey = idempotencyKey;
    }

    /**
     * State machine — only valid transitions allowed.
     * CREATED → SHIPPED
     * CREATED → CANCELLED
     */
    public void ship() {
        if (this.status != OrderStatus.CREATED) {
            throw new InvalidOrderStatusTransitionException(this.status, OrderStatus.SHIPPED);
        }
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status != OrderStatus.CREATED) {
            throw new InvalidOrderStatusTransitionException(this.status, OrderStatus.CANCELLED);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getAddressId() { return addressId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
}