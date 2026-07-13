package com.demo.order_service.application.port.in;

import java.util.UUID;

import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderStatus;

public interface UpdateOrderStatusUseCase {
 
    record Command(UUID orderId, OrderStatus newStatus) {}
 
    Order updateStatus(Command command);
}

