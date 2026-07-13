package com.demo.customer_service.application.port.in;

import java.util.List;

import com.demo.customer_service.domain.model.Customer;

public interface CreateCustomerUseCase {

    record Command(String firstName, String lastName, String email) {}

    Customer createCustomer(Command command); // domain Order return karta hai

    List<Customer> getAllCustomers();
}
