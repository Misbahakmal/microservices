package com.demo.customer_service.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.demo.customer_service.application.port.out.AddressRepositoryPort;
import com.demo.customer_service.domain.model.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AddressPersistenceAdapter implements AddressRepositoryPort {

    private final AddressSpringDataRepository addressRepository;
    private final CustomerSpringDataRepository customerRepository;

    public AddressPersistenceAdapter(AddressSpringDataRepository addressRepository,
                                     CustomerSpringDataRepository customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Address save(Address address) {
        CustomerJpaEntity customerRef = customerRepository.getReferenceById(address.getCustomerId());
        AddressJpaEntity entity = new AddressJpaEntity(
                address.getId(),
                customerRef,
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry()
        );
        AddressJpaEntity saved = addressRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Address> findByCustomerId(UUID customerId) {
        return addressRepository.findByCustomerId(customerId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Address toDomain(AddressJpaEntity entity) {
        return new Address(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getStreet(),
                entity.getCity(),
                entity.getPostalCode(),
                entity.getCountry()
        );
    }



   @Override
public Optional<Address> findByIdAndCustomerId(UUID addressId, UUID customerId) {
    return addressRepository.findByIdAndCustomerId(addressId, customerId)
            .map(this::toDomain); 
}
}
