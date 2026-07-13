package com.demo.order_service.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.demo.order_service.domain.model.OrderStatus;

@Entity
@Table(name = "orders", schema = "order_schema")
public class OrderJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp // Khud hi time set karega naye record par
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp //// Har update par khud hi time badlega
    private Instant updatedAt;

    // Lifecycle hooks for automatic timestamps
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    protected OrderJpaEntity() {}

    public OrderJpaEntity(UUID id, UUID customerId, UUID addressId, OrderStatus status,
                          String idempotencyKey, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.addressId = addressId;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getAddressId() { return addressId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItemJpaEntity> getItems() { return items; }
    public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
}
}