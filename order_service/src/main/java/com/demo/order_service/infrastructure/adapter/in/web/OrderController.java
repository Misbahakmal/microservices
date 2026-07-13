package com.demo.order_service.infrastructure.adapter.in.web;
import com.demo.order_service.application.port.in.CreateOrderUseCase;
import com.demo.order_service.application.port.in.GetOrderUseCase;
import com.demo.order_service.application.port.in.UpdateOrderStatusUseCase;
import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderItem;
import com.demo.order_service.infrastructure.adapter.in.web.model.CreateOrderRequest;
import com.demo.order_service.infrastructure.adapter.in.web.model.OrderItemResponse;
import com.demo.order_service.infrastructure.adapter.in.web.model.OrderResponse;
import com.demo.order_service.infrastructure.adapter.in.web.model.OrderStatus;
import com.demo.order_service.infrastructure.adapter.in.web.model.UpdateOrderStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    // ── Exact signature from OrdersApi ──────────────────────────────────────
    // idempotencyKey: UUID (not String), required = true (not optional)
    // createOrderRequest: exact parameter name from interface
    // throws Exception: interface mein hai, override mein bhi hona chahiye

    @Override
    public ResponseEntity<OrderResponse> createOrder(
            UUID idempotencyKey,
            CreateOrderRequest createOrderRequest) throws Exception {

        List<CreateOrderUseCase.OrderItemCommand> itemCommands =
                createOrderRequest.getItems().stream()
                        .map(i -> new CreateOrderUseCase.OrderItemCommand(
                                i.getProductId(),
                                i.getProductName(),
                                i.getQuantity(),
                                i.getUnitPrice()
                        ))
                        .collect(Collectors.toList());

        var command = new CreateOrderUseCase.Command(
                createOrderRequest.getCustomerId(),
                createOrderRequest.getAddressId(),
                itemCommands,
                idempotencyKey != null ? idempotencyKey.toString() : null
        );

        Order order = createOrderUseCase.createOrder(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(order));
    }

    // ── Exact signature from OrdersApi ──────────────────────────────────────
    // UUID customerId, OrderStatus status — same types
    // throws Exception — interface mein hai

    @Override
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            UUID customerId,
            OrderStatus status) throws Exception {

        com.demo.order_service.domain.model.OrderStatus domainStatus = status != null
                ? com.demo.order_service.domain.model.OrderStatus.valueOf(status.name())
                : null;

        List<OrderResponse> responses = getOrderUseCase
                .getAllOrders(customerId, domainStatus)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // ── Exact signature from OrdersApi ──────────────────────────────────────

    @Override
    public ResponseEntity<OrderResponse> getOrderById(UUID id) throws Exception {
        Order order = getOrderUseCase.getOrderById(id);
        return ResponseEntity.ok(toResponse(order));
    }

    // ── Exact signature from OrdersApi ──────────────────────────────────────
    // parameter name: updateOrderStatusRequest (interface se match)

    @Override
    public ResponseEntity<OrderResponse> updateOrderStatus(
            UUID id,
            UpdateOrderStatusRequest updateOrderStatusRequest) throws Exception {

        com.demo.order_service.domain.model.OrderStatus domainStatus =
                com.demo.order_service.domain.model.OrderStatus
                        .valueOf(updateOrderStatusRequest.getStatus().name());

        var command = new UpdateOrderStatusUseCase.Command(id, domainStatus);
        Order order = updateOrderStatusUseCase.updateStatus(command);

        return ResponseEntity.ok(toResponse(order));
    }

    // ── Mapping: domain → generated DTOs ────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        return new OrderResponse()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .addressId(order.getAddressId())
                .status(order.getStatus() != null
                        ? OrderStatus.fromValue(order.getStatus().name())
                        : null)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt() != null
                        ? order.getCreatedAt().atOffset(ZoneOffset.UTC)
                        : null)
                .updatedAt(order.getUpdatedAt() != null
                        ? order.getUpdatedAt().atOffset(ZoneOffset.UTC)
                        : null)
                .items(order.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()));
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse()
                //.id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice());
    }
}