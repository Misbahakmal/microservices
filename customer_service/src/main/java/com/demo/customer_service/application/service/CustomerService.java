package com.demo.customer_service.application.service;


import com.demo.customer_service.application.port.in.CreateCustomerUseCase;
import com.demo.customer_service.application.port.in.GetCustomerUseCase;
import com.demo.customer_service.domain.model.Customer;
import com.demo.customer_service.domain.model.CustomerEmailAlreadyExistsException;
import com.demo.customer_service.domain.model.CustomerNotFoundException;
import com.demo.customer_service.application.port.out.CustomerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 I willl not use @Repository, no @Entity here.// port implement karta hai jitny b usecases han jo k in port ma han 
 */
@Service
@Transactional
public class CustomerService implements CreateCustomerUseCase, GetCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;

    public CustomerService(CustomerRepositoryPort customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer createCustomer(Command command) {
        if (customerRepository.existsByEmail(command.email())) {
            throw new CustomerEmailAlreadyExistsException(command.email());
        }

        Customer customer = new Customer(command.firstName(), command.lastName(), command.email());
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}
