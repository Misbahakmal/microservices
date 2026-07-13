package com.demo.customer_service.infrastructure.adapter.in.web;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.demo.customer_service.application.port.in.AddressUseCase;
import com.demo.customer_service.application.port.in.CreateCustomerUseCase;
import com.demo.customer_service.application.port.in.GetCustomerUseCase;
import com.demo.customer_service.domain.model.Address;
import com.demo.customer_service.domain.model.Customer;
import com.demo.customer_service.infrastructure.adapter.in.web.model.AddressResponse;
import com.demo.customer_service.infrastructure.adapter.in.web.model.CreateAddressRequest;
import com.demo.customer_service.infrastructure.adapter.in.web.model.CreateCustomerRequest;
import com.demo.customer_service.infrastructure.adapter.in.web.model.CustomerResponse;
import lombok.RequiredArgsConstructor;
// Generated interface implement krta ha jo k plugin k through bnaya tha;
// Yeh woh jagah hai jo generated DTOs aur domain world ko connect karti hai:
// Controller sirf yeh do kaam karta hai: generated DTO ko Command mein convert karo, 
// phir domain Order ko generated OrderResponse mein convert karo.
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final AddressUseCase addressUseCase;

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CreateCustomerRequest request) throws Exception {
        var command = new CreateCustomerUseCase.Command(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );
        
        Customer customer = createCustomerUseCase.createCustomer(command);
        
        // FRom API Spec
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(customer));
    }

    @Override
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() throws Exception {
        // Calling getAllCustomers from CreateCustomerUseCase from my interface
        List<CustomerResponse> responses = createCustomerUseCase.getAllCustomers().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(UUID id) throws Exception {
        Customer customer = getCustomerUseCase.getCustomerById(id);
        return ResponseEntity.ok(toResponse(customer));
    }

    @Override
    public ResponseEntity<AddressResponse> addAddress(UUID id,CreateAddressRequest request) throws Exception {
        var command = new AddressUseCase.AddAddressCommand(
                id,
                request.getStreet(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
        
        Address address = addressUseCase.addAddress(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAddressResponse(address));
    }

    @Override
    public ResponseEntity<List<AddressResponse>> getAddressesByCustomerId(UUID id) throws Exception {
        // Using the single AddressUseCase for queries
        List<AddressResponse> responses = addressUseCase.getAddressesByCustomerId(id).stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @Override
    public ResponseEntity<AddressResponse> getAddressById(UUID id, UUID addressId) throws Exception {
    Address address = addressUseCase.getAddressByCustomerIdAndAddressId(id, addressId);
    return ResponseEntity.ok(toAddressResponse(address));
}


    // --- Mappers ---Domain object ko api response ma convert krta ha 

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        if (customer.getCreatedAt() != null) {
            response.setCreatedAt(customer.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return response;
    }

    private AddressResponse toAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setCustomerId(address.getCustomerId());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        return response;
    }
}