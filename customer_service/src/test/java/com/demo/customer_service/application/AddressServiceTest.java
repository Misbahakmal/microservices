package com.demo.customer_service.application;
import com.demo.customer_service.application.port.in.AddressUseCase;
import com.demo.customer_service.application.port.out.AddressRepositoryPort;
import com.demo.customer_service.application.port.out.CustomerRepositoryPort;
import com.demo.customer_service.application.service.AddressService;
import com.demo.customer_service.domain.model.Address;
import com.demo.customer_service.domain.model.AddressNotFoundException;
import com.demo.customer_service.domain.model.Customer;
import com.demo.customer_service.domain.model.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepositoryPort addressRepository;

    @Mock
    private CustomerRepositoryPort customerRepository;

    private AddressService addressService;

    // --- addAddress ---

    @Test
    void addAddress_savesAddress_whenCustomerExists() {
        addressService = new AddressService(addressRepository, customerRepository);
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("John", "Doe", "john@example.com");
        var command = new AddressUseCase.AddAddressCommand(
                customerId, "Carnotstr. 4", "Berlin", "10587", "DE");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Address result = addressService.addAddress(command);

        assertThat(result.getStreet()).isEqualTo("Carnotstr. 4");
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void addAddress_throws_whenCustomerDoesNotExist() {
        addressService = new AddressService(addressRepository, customerRepository);
        UUID customerId = UUID.randomUUID();
        var command = new AddressUseCase.AddAddressCommand(
                customerId, "Carnotstr. 4", "Berlin", "10587", "DE");

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.addAddress(command))
                .isInstanceOf(CustomerNotFoundException.class);

        // Same principle as the duplicate-email test: prove that an
        // invalid parent reference never reaches the address repository.
        verify(addressRepository, never()).save(any(Address.class));
    }

    // --- getAddressesByCustomerId ---

    @Test
    void getAddressesByCustomerId_returnsList_whenCustomerExists() {
        addressService = new AddressService(addressRepository, customerRepository);
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("John", "Doe", "john@example.com");
        Address address = new Address(customerId, "Street 1", "Berlin", "10115", "DE");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findByCustomerId(customerId)).thenReturn(List.of(address));

        List<Address> result = addressService.getAddressesByCustomerId(customerId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAddressesByCustomerId_throws_whenCustomerDoesNotExist() {
        addressService = new AddressService(addressRepository, customerRepository);
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressesByCustomerId(customerId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    // --- getAddressByCustomerIdAndAddressId (the endpoint built earlier) ---

    @Test
    void getAddressByCustomerIdAndAddressId_returnsAddress_whenItBelongsToCustomer() {
        addressService = new AddressService(addressRepository, customerRepository);
        UUID customerId = UUID.randomUUID();
        Address address = new Address(customerId, "Carnotstr. 4", "Berlin", "10587", "DE");
        UUID addressId = address.getId();

        when(addressRepository.findByIdAndCustomerId(addressId, customerId))
                .thenReturn(Optional.of(address));

        Address result = addressService.getAddressByCustomerIdAndAddressId(customerId, addressId);

        assertThat(result.getStreet()).isEqualTo("Carnotstr. 4");
    }

    @Test
    void getAddressByCustomerIdAndAddressId_throws_whenAddressDoesNotBelongToCustomer() {
        addressService = new AddressService(addressRepository, customerRepository);
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        // simulates: address exists, but for a DIFFERENT customer, or doesn't
        // exist at all - the repository query returns empty in both cases
        when(addressRepository.findByIdAndCustomerId(addressId, customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressByCustomerIdAndAddressId(customerId, addressId))
                .isInstanceOf(AddressNotFoundException.class);
    }
}