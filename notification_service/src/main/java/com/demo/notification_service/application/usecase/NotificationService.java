package com.demo.notification_service.application.usecase;

import java.time.Instant;
import java.util.UUID;

import com.demo.notification_service.application.port.out.NotificationRepositoryPort;
import com.demo.notification_service.domain.Notification;

public class NotificationService { // No @Service here!

    private final NotificationRepositoryPort repositoryPort;

    public NotificationService(NotificationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    public void processEvent(UUID eventId, UUID orderId, String status) {
        if (repositoryPort.existsByEventId(eventId)) {
            return;
        }

        Notification notification = new Notification(
                UUID.randomUUID(),
                eventId,
                orderId,
                "Order " + orderId + " updated to " + status,
                Instant.now()
        );

        repositoryPort.save(notification);
    }
}
