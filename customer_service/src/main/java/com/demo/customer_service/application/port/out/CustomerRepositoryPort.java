package com.demo.customer_service.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.demo.customer_service.domain.model.Customer;

/**
 * Outgoing port .gt data from database
 * The application layer depends on this interface; infrastructure implements it.
 */
public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByEmail(String email);

    List<Customer> findAll();

    boolean existsByEmail(String email);
   // boolean deleteCustomer(UUID id);  i did not addded this method in apispec file so first i have to add file then perform
}
