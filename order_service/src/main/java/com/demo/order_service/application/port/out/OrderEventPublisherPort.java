package com.demo.order_service.application.port.out;

import com.demo.order_service.domain.model.OrderEvent;

/**
 * Outgoing port for publishing order events.
 * The application layer depends on this; Kafka adapter implements it.
 */
public interface OrderEventPublisherPort {
 
    void publish(OrderEvent event, String correlationId);
}

