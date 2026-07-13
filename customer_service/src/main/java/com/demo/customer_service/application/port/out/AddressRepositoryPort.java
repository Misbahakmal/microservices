package com.demo.customer_service.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.demo.customer_service.domain.model.Address;
//use case database se baat aise karta hai q k usecase ko database ka koi idea nh ha 
public interface AddressRepositoryPort {

    Address save(Address address);

    List<Address> findByCustomerId(UUID customerId);
    Optional<Address> findByIdAndCustomerId(UUID addressId, UUID customerId);
}
