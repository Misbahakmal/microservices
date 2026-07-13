package com.demo.notification_service.infrastructure.adapters.out.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demo.notification_service.application.port.out.NotificationRepositoryPort;
import com.demo.notification_service.application.usecase.NotificationService;

@Configuration
public class ServiceConfig {

    @Bean
    public NotificationService notificationService(NotificationRepositoryPort repositoryPort) {
        // Yahan hum manual object bana rahe hain taaki Use Case clean rahe
        return new NotificationService(repositoryPort);
    }
}