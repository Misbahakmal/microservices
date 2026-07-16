package com.demo.notification_service.application.usecase;

import com.demo.notification_service.application.port.out.NotificationRepositoryPort;
import com.demo.notification_service.domain.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Plain unit test - NotificationService has no @Service annotation, so it's
// just a regular Java object here, constructed directly with a mocked port.
// No Spring context needed at all for this layer.
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepositoryPort repositoryPort;

    private NotificationService notificationService;

    @Test
    void processEvent_savesNotification_whenEventIsNew() {
        notificationService = new NotificationService(repositoryPort);
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(repositoryPort.existsByEventId(eventId)).thenReturn(false);

        notificationService.processEvent(eventId, orderId, "SHIPPED");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repositoryPort).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getMessage()).contains(orderId.toString()).contains("SHIPPED");
    }

    @Test
    void processEvent_doesNothing_whenEventAlreadyProcessed() {
        // This is the most important test in this class: it proves
        // idempotency. Kafka guarantees "at-least-once" delivery, meaning
        // the SAME event can legitimately arrive twice (consumer rebalance,
        // retry after a slow ack, etc.) - this is not an edge case, it's
        // expected behavior that must be handled correctly.
        notificationService = new NotificationService(repositoryPort);
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(repositoryPort.existsByEventId(eventId)).thenReturn(true);

        notificationService.processEvent(eventId, orderId, "SHIPPED");

        // Critical assertion: on a duplicate event, save() must NEVER be
        // called - otherwise the customer gets a duplicate notification,
        // or the database ends up with duplicate rows.
        verify(repositoryPort, never()).save(any(Notification.class));
    }

    @Test
    void processEvent_checksIdempotencyBeforeSaving() {
        // Verifies the ORDER of operations, not just the outcome - the
        // existence check must happen before any save is attempted.
        notificationService = new NotificationService(repositoryPort);
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(repositoryPort.existsByEventId(eventId)).thenReturn(false);

        notificationService.processEvent(eventId, orderId, "CREATED");

        var inOrder = inOrder(repositoryPort);
        inOrder.verify(repositoryPort).existsByEventId(eventId);
        inOrder.verify(repositoryPort).save(any(Notification.class));
    }

    @Test
    void processEvent_buildsReadableMessage_forEachStatus() {
        notificationService = new NotificationService(repositoryPort);
        UUID orderId = UUID.randomUUID();

        for (String status : new String[]{"CREATED", "SHIPPED", "CANCELLED"}) {
            UUID eventId = UUID.randomUUID();
            when(repositoryPort.existsByEventId(eventId)).thenReturn(false);

            notificationService.processEvent(eventId, orderId, status);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(repositoryPort, atLeastOnce()).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getMessage()).contains(status);
        }
    }
}
