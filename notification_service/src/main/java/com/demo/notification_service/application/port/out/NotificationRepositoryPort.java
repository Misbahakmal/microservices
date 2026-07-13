package com.demo.notification_service.application.port.out;

import java.util.UUID;

import com.demo.notification_service.domain.Notification;

public interface NotificationRepositoryPort {
    void save(Notification notification);
    boolean existsByEventId(UUID eventId);
}