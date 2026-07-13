package com.demo.customer_service.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demo.customer_service.application.port.in.AddressUseCase;
import com.demo.customer_service.application.port.out.AddressRepositoryPort;
import com.demo.customer_service.application.port.out.CustomerRepositoryPort;
import com.demo.customer_service.domain.model.Address;
import com.demo.customer_service.domain.model.AddressNotFoundException;
import com.demo.customer_service.domain.model.CustomerNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AddressService implements AddressUseCase {

    private final AddressRepositoryPort addressRepository;
    private final CustomerRepositoryPort customerRepository;

    public AddressService(AddressRepositoryPort addressRepository,
                          CustomerRepositoryPort customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Address addAddress(AddAddressCommand command) {
        // Validate customer exists before adding address
        customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        Address address = new Address(
                command.customerId(),
                command.street(),
                command.city(),
                command.postalCode(),
                command.country()
        );
        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByCustomerId(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return addressRepository.findByCustomerId(customerId);
    }

   @Override
public Address getAddressByCustomerIdAndAddressId(UUID customerId, UUID addressId) {
    return addressRepository.findByIdAndCustomerId(addressId, customerId)
            .orElseThrow(() -> new AddressNotFoundException(customerId, addressId));
}
}
