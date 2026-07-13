package com.demo.customer_service.application.port.in;

import java.util.UUID;

import com.demo.customer_service.domain.model.Customer;

public interface GetCustomerUseCase {

    Customer getCustomerById(UUID id);
}
