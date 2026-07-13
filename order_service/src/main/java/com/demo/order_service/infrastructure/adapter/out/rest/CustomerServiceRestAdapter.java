package com.demo.order_service.infrastructure.adapter.out.rest;

import com.demo.order_service.application.port.out.CustomerServicePort;
import com.demo.order_service.domain.model.CustomerAddress;
import com.demo.order_service.domain.model.CustomerValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CustomerServiceRestAdapter implements CustomerServicePort {
 
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceRestAdapter.class);
 
    private final CustomerFeignClient customerFeignClient;
 
    public CustomerServiceRestAdapter(CustomerFeignClient customerFeignClient) {
        this.customerFeignClient = customerFeignClient;
    }
 
    @Override
    public CustomerAddress fetchValidatedCustomerAddress(UUID customerId, UUID addressId) {
        try {
            CustomerFeignClient.AddressDto address = customerFeignClient.getAddressByCustomerIdAndAddressId(customerId, addressId);
 
            if (address == null) {
                throw new CustomerValidationException(customerId, "Address response was empty");
            }
 
            return new CustomerAddress(
                    customerId,
                    addressId,
                    address.street(),
                    address.city(),
                    address.postalCode(),
                    address.country()
            );
        } catch (CustomerValidationException e) {
            // Thrown by CustomerFeignErrorDecoder (404/5xx) with null customerId
            // context — re-throw here with the real customerId attached.
            throw new CustomerValidationException(customerId, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to fetch customer address for customerId={} addressId={}", customerId, addressId, e);
            throw new CustomerValidationException(customerId,
                    "Customer service unreachable: " + e.getMessage());
        }
    }



  
}
 