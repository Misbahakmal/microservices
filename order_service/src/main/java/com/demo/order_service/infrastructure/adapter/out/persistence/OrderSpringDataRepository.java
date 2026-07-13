package com.demo.order_service.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.order_service.domain.model.OrderStatus;

interface OrderSpringDataRepository extends JpaRepository<OrderJpaEntity, UUID> {
 
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);
 
    @Query("SELECT o FROM OrderJpaEntity o WHERE " +
           "(:customerId IS NULL OR o.customerId = :customerId) AND " +
           "(:status IS NULL OR o.status = :status)")
    List<OrderJpaEntity> findAllWithFilters(@Param("customerId") UUID customerId,
                                            @Param("status") OrderStatus status);
}

