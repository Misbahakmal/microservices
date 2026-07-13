package com.demo.order_service.application.port.in;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.demo.order_service.domain.model.Order;

public interface CreateOrderUseCase {

    record OrderItemCommand(UUID productId, String productName, int quantity, BigDecimal unitPrice) {}

    record Command(UUID customerId, UUID addressId, List<OrderItemCommand> items, String idempotencyKey) {}

    Order createOrder(Command command);
}