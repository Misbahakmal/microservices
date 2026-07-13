package com.demo.notification_service.infrastructure.adapters.out.persistence;
import org.springframework.stereotype.Component;

import com.demo.notification_service.application.port.out.NotificationRepositoryPort;
import com.demo.notification_service.domain.Notification;

import java.util.UUID;

@Component
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository repository;

    // Manual Constructor (Lombok ke bagair, error nahi aayega)
    public NotificationPersistenceAdapter(JpaNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public void save(Notification notification) {
        // Builder ki jagah Manual Constructor use ho raha hai
        NotificationEntity entity = new NotificationEntity(
                notification.getId(),
                notification.getEventId(),
                notification.getOrderId(),
                notification.getMessage(),
                notification.getSentAt()
        );

        repository.save(entity);
    }
}