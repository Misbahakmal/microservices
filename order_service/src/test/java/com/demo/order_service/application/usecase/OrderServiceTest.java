package com.demo.order_service.application.usecase;

import com.demo.order_service.application.port.in.CreateOrderUseCase;
import com.demo.order_service.application.port.in.UpdateOrderStatusUseCase;
import com.demo.order_service.application.port.out.CustomerServicePort;
import com.demo.order_service.application.port.out.OrderEventPublisherPort;
import com.demo.order_service.application.port.out.OrderRepositoryPort;
import com.demo.order_service.domain.model.CustomerAddress;
import com.demo.order_service.domain.model.CustomerValidationException;
import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderEvent;
import com.demo.order_service.domain.model.OrderNotFoundException;
import com.demo.order_service.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
 
    @Mock
    private OrderRepositoryPort orderRepository;
 
    @Mock
    private CustomerServicePort customerService;
 
    @Mock
    private OrderEventPublisherPort eventPublisher;
 
    private OrderService orderService;
 
    private UUID customerId;
    private UUID addressId;
    private List<CreateOrderUseCase.OrderItemCommand> items;
 
    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, customerService, eventPublisher);
        customerId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        items = List.of(new CreateOrderUseCase.OrderItemCommand(
                UUID.randomUUID(), "Widget", 2, new BigDecimal("9.99")));
    }
 
    // ─── createOrder ────────────────────────────────────────────────────
 
    @Test
    void createOrder_validatesCustomer_savesOrder_andPublishesCreatedEvent() {
        var command = new CreateOrderUseCase.Command(customerId, addressId, items, "idem-key-1");
 
        when(orderRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.empty());
        when(customerService.fetchValidatedCustomerAddress(customerId, addressId))
                .thenReturn(new CustomerAddress(customerId, addressId, "Street 1", "Berlin", "10115", "DE"));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
 
        Order result = orderService.createOrder(command);
 
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
 
        // Order matters here: customer must be validated BEFORE the order is
        // ever persisted - this is the exact business rule the code comments
        // describe ("Validate customer and address BEFORE adding to database").
        var inOrder = inOrder(customerService, orderRepository, eventPublisher);
        inOrder.verify(customerService).fetchValidatedCustomerAddress(customerId, addressId);
        inOrder.verify(orderRepository).save(any(Order.class));
        inOrder.verify(eventPublisher).publish(any(OrderEvent.class), eq(result.getId().toString()));
    }
 
    @Test
    void createOrder_returnsExistingOrder_whenIdempotencyKeyAlreadyUsed() {
        var command = new CreateOrderUseCase.Command(customerId, addressId, items, "idem-key-1");
        Order existingOrder = Order.create(customerId, addressId,
                List.of(new com.demo.order_service.domain.model.OrderItem(
                        UUID.randomUUID(), "Widget", 2, new BigDecimal("9.99"))),
                "idem-key-1");
 
        when(orderRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.of(existingOrder));
 
        Order result = orderService.createOrder(command);
 
        assertThat(result).isEqualTo(existingOrder);
 
        // Critical assertion: on a duplicate request, NOTHING else should
        // happen - no re-validation, no second save, no duplicate event.
        // This is what actually makes the operation idempotent.
        verify(customerService, never()).fetchValidatedCustomerAddress(any(), any());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publish(any(), any());
    }
 
    @Test
    void createOrder_skipsIdempotencyCheck_whenKeyIsNull() {
        var command = new CreateOrderUseCase.Command(customerId, addressId, items, null);
 
        when(customerService.fetchValidatedCustomerAddress(customerId, addressId))
                .thenReturn(new CustomerAddress(customerId, addressId, "Street 1", "Berlin", "10115", "DE"));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
 
        orderService.createOrder(command);
 
        // With a null key, the repository should never be asked to look up
        // by idempotency key at all - not even with a null argument.
        verify(orderRepository, never()).findByIdempotencyKey(any());
        verify(orderRepository).save(any(Order.class));
    }
 
    @Test
    void createOrder_throws_andNeverSavesOrPublishes_whenCustomerValidationFails() {
        var command = new CreateOrderUseCase.Command(customerId, addressId, items, "idem-key-2");
 
        when(orderRepository.findByIdempotencyKey("idem-key-2")).thenReturn(Optional.empty());
        when(customerService.fetchValidatedCustomerAddress(customerId, addressId))
                .thenThrow(new CustomerValidationException(customerId, "Address not found for customer"));
 
        assertThatThrownBy(() -> orderService.createOrder(command))
                .isInstanceOf(CustomerValidationException.class);
 
        // If customer/address validation fails, the order must never reach
        // the database and no event should ever be published for it.
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publish(any(), any());
    }
 
    // ─── getOrderById ───────────────────────────────────────────────────
 
    @Test
    void getOrderById_returnsOrder_whenFound() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.create(customerId, addressId,
                List.of(new com.demo.order_service.domain.model.OrderItem(
                        UUID.randomUUID(), "Widget", 1, new BigDecimal("5.00"))),
                null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
 
        Order result = orderService.getOrderById(orderId);
 
        assertThat(result).isEqualTo(order);
    }
 
    @Test
    void getOrderById_throws_whenNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
 
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }
 
    // ─── getAllOrders ───────────────────────────────────────────────────
 
    @Test
    void getAllOrders_delegatesFilterToRepository() {
        Order order = Order.create(customerId, addressId,
                List.of(new com.demo.order_service.domain.model.OrderItem(
                        UUID.randomUUID(), "Widget", 1, new BigDecimal("5.00"))),
                null);
        when(orderRepository.findAll(customerId, OrderStatus.CREATED)).thenReturn(List.of(order));
 
        List<Order> result = orderService.getAllOrders(customerId, OrderStatus.CREATED);
 
        assertThat(result).hasSize(1);
        verify(orderRepository).findAll(customerId, OrderStatus.CREATED);
    }
 
    // ─── updateStatus ───────────────────────────────────────────────────
 
    @Test
    void updateStatus_shipsOrder_andPublishesShippedEvent() {
        Order order = Order.create(customerId, addressId,
                List.of(new com.demo.order_service.domain.model.OrderItem(
                        UUID.randomUUID(), "Widget", 1, new BigDecimal("5.00"))),
                null);
        UUID orderId = order.getId(); // use the ID Order.create() actually generated
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
 
        var command = new UpdateOrderStatusUseCase.Command(orderId, OrderStatus.SHIPPED);
        Order result = orderService.updateStatus(command);
 
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(eventPublisher).publish(
                argThat((OrderEvent e) -> e.getStatus() == OrderStatus.SHIPPED),
                eq(orderId.toString()));
    }
 
    @Test
    void updateStatus_cancelsOrder_andPublishesCancelledEvent() {
        Order order = Order.create(customerId, addressId,
                List.of(new com.demo.order_service.domain.model.OrderItem(
                        UUID.randomUUID(), "Widget", 1, new BigDecimal("5.00"))),
                null);
        UUID orderId = order.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
 
        var command = new UpdateOrderStatusUseCase.Command(orderId, OrderStatus.CANCELLED);
        Order result = orderService.updateStatus(command);
 
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
 
    @Test
    void updateStatus_throws_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
 
        var command = new UpdateOrderStatusUseCase.Command(orderId, OrderStatus.SHIPPED);
 
        assertThatThrownBy(() -> orderService.updateStatus(command))
                .isInstanceOf(OrderNotFoundException.class);
    }
 
    @Test
    void updateStatus_throws_whenTargetStatusIsNotShippedOrCancelled() {
        Order order = Order.create(customerId, addressId,
                List.of(new com.demo.order_service.domain.model.OrderItem(
                        UUID.randomUUID(), "Widget", 1, new BigDecimal("5.00"))),
                null);
        UUID orderId = order.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
 
        // CREATED is not a valid manual target - only SHIPPED/CANCELLED are,
        // per the switch statement's default branch in OrderService.
        var command = new UpdateOrderStatusUseCase.Command(orderId, OrderStatus.CREATED);
 
        assertThatThrownBy(() -> orderService.updateStatus(command))
                .isInstanceOf(IllegalArgumentException.class);
 
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publish(any(), any());
    }
}