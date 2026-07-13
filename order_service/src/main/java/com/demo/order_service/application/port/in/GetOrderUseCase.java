package com.demo.order_service.application.port.in;
import java.util.List;
import java.util.UUID;

import com.demo.order_service.domain.model.Order;
import com.demo.order_service.domain.model.OrderStatus;

public interface GetOrderUseCase {
 
    Order getOrderById(UUID id);
 
    List<Order> getAllOrders(UUID customerId, OrderStatus status);
}
