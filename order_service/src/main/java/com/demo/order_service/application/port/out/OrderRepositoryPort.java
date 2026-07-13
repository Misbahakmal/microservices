package com.demo.order_service.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderStatus;

public interface OrderRepositoryPort {
 
    Order save(Order order);
 
    Optional<Order> findById(UUID id);
 
    List<Order> findAll(UUID customerId, OrderStatus status);
 
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
