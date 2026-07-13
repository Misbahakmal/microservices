package com.demo.order_service.application.usecase;

import com.demo.order_service.application.port.in.CreateOrderUseCase;
import com.demo.order_service.application.port.in.GetOrderUseCase;
import com.demo.order_service.application.port.in.UpdateOrderStatusUseCase;
import com.demo.order_service.application.port.out.CustomerServicePort;
import com.demo.order_service.application.port.out.OrderEventPublisherPort;
import com.demo.order_service.application.port.out.OrderRepositoryPort;
import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderEvent;
import com.demo.order_service.domain.model.OrderItem;
import com.demo.order_service.domain.model.OrderNotFoundException;
import com.demo.order_service.domain.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order use case implementation.
 * Orchestrates: customer validation → persist order → publish event.
 * Transactional guarantee: DB commit happens before event is published.
 */
@Service
@Transactional
public class OrderService implements CreateOrderUseCase, GetOrderUseCase, UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CustomerServicePort customerService;
    private final OrderEventPublisherPort eventPublisher;

    public OrderService(OrderRepositoryPort orderRepository,
                        CustomerServicePort customerService,
                        OrderEventPublisherPort eventPublisher) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Order createOrder(CreateOrderUseCase.Command command) {
        // Idempotency check — return existing order if key was already used
        if (command.idempotencyKey() != null) {
            var existing = orderRepository.findByIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Validate customer and address BEFORE adding to database
        customerService.fetchValidatedCustomerAddress(command.customerId(), command.addressId());

        List<OrderItem> items = command.items().stream()
                .map(i -> new OrderItem(i.productId(), i.productName(), i.quantity(), i.unitPrice()))
                .collect(Collectors.toList());
                
            // here i am also sending idempotency key for unique order
            Order order = Order.create(command.customerId(), command.addressId(), items,
                           command.idempotencyKey());

        // B comit first— then publish event 
        Order saved = orderRepository.save(order);

        // Event published AFTER successful DB commit via @TransactionalEventListener
        // or within the same Kafka transaction (configured in adapter)
        eventPublisher.publish(
                new OrderEvent(saved.getId(), OrderStatus.CREATED),
                saved.getId().toString()
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders(UUID customerId, OrderStatus status) {
        return orderRepository.findAll(customerId, status);
    }

    @Override
    public Order updateStatus(UpdateOrderStatusUseCase.Command command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        // Apply state machine transition (throws on invalid transition)
        switch (command.newStatus()) {
            case SHIPPED -> order.ship();
            case CANCELLED -> order.cancel();
            default -> throw new IllegalArgumentException(
                    "Cannot manually set status to: " + command.newStatus());
        }

        Order saved = orderRepository.save(order);

        // Publish event after status change
        eventPublisher.publish(
                new OrderEvent(saved.getId(), saved.getStatus()),
                saved.getId().toString()
        );

        return saved;
    }

}