package com.demo.customer_service.application.port.in;

import java.util.List;
import java.util.UUID;

import com.demo.customer_service.domain.model.Address;
// Incoming port — controller yahan se kaam karwata hai
public interface AddressUseCase {

    record AddAddressCommand(UUID customerId, String street, String city,
                             String postalCode, String country) {}

    Address addAddress(AddAddressCommand command);

    List<Address> getAddressesByCustomerId(UUID customerId);
    Address getAddressByCustomerIdAndAddressId(UUID customerId, UUID addressId);
    
}
