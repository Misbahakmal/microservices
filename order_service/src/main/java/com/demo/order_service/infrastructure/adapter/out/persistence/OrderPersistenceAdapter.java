package com.demo.order_service.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.demo.order_service.application.port.out.OrderRepositoryPort;
import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderItem;
import com.demo.order_service.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderSpringDataRepository springDataRepository;

    public OrderPersistenceAdapter(OrderSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Order save(Order order) {
        // Check if entity already exists (update vs insert)
        OrderJpaEntity entity = springDataRepository.findById(order.getId())
                .map(existing -> {
                    existing.setStatus(order.getStatus());
                    existing.setUpdatedAt(order.getUpdatedAt());
                    return existing;
                })
                .orElseGet(() -> {
                    OrderJpaEntity newEntity = new OrderJpaEntity(
                            order.getId(),
                            order.getCustomerId(),
                            order.getAddressId(),
                            order.getStatus(),
                            order.getIdempotencyKey(), // idempotency key stored separately
                            order.getCreatedAt(),
                            order.getUpdatedAt()
                    );
                    // Add items
                    order.getItems().forEach(item -> {
                        OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                               // item.getId(),
                                newEntity,
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        );
                        newEntity.getItems().add(itemEntity);
                    });
                    return newEntity;
                });

        OrderJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Order> findAll(UUID customerId, OrderStatus status) {
        return springDataRepository.findAllWithFilters(customerId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    // --- Mapping ---

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(),
                        i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList());

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getAddressId(),
                entity.getStatus(),
                items,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIdempotencyKey()
        );
    }
}
