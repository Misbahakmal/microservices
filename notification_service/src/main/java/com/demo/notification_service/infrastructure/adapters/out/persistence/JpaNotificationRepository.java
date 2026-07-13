package com.demo.notification_service.infrastructure.adapters.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    
    // Idempotency check karne ke liye helper method
    boolean existsByEventId(UUID eventId);
}
