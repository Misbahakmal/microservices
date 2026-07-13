package com.demo.customer_service.infrastructure.adapter.out.persistence;


import org.springframework.stereotype.Component;

import com.demo.customer_service.application.port.out.CustomerRepositoryPort;
import com.demo.customer_service.domain.model.Address;
import com.demo.customer_service.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter: implements the outgoing CustomerRepositoryPort.
 * Translates between domain objects and JPA entities.
 */
@Component
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerSpringDataRepository springDataRepository;

    public CustomerPersistenceAdapter(CustomerSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = toJpaEntity(customer);
        //domain object to ly k db ma save krna
        CustomerJpaEntity saved = springDataRepository.save(entity);
        //fr wapis domain object return krna 
        return toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return springDataRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return springDataRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    // --- Mapping ---

    private CustomerJpaEntity toJpaEntity(Customer customer) {
        return new CustomerJpaEntity(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getCreatedAt()
        );
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        List<Address> addresses = entity.getAddresses().stream()
                .map(this::addressToDomain)
                .collect(Collectors.toList());
        return new Customer(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getCreatedAt(),
                addresses
        );
    }

    private Address addressToDomain(AddressJpaEntity e) {
        return new Address(
                e.getId(),
                e.getCustomer().getId(),
                e.getStreet(),
                e.getCity(),
                e.getPostalCode(),
                e.getCountry()
        );
    }

    // @Override
    // public boolean deleteCustomer(UUID id) {
    //    CustomerJpaEntity cust = springDataRepository.findById(id).get();
    //     springDataRepository.delete(cust);
    //     return true;
    // simple 
    //springDataRepository.deleteById(id);
        
    // }
}
