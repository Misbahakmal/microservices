package com.demo.order_service.infrastructure.adapter.out.rest;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// @FeignClient(
//     name = "customer-service",
//     url = "${services.customer.base-url}"
// )
@FeignClient(name = "customer-service")
public interface CustomerFeignClient {
 
    @GetMapping("/customers/{id}/addresses/{addressId}")
    AddressDto getAddressByCustomerIdAndAddressId(@PathVariable("id") UUID customerId,
                                                    @PathVariable("addressId") UUID addressId);
 
    record AddressDto(String id, String customerId, String street, String city, String postalCode, String country) {}
}