package com.demo.customer_service.application;
import com.demo.customer_service.application.port.in.CreateCustomerUseCase;
import com.demo.customer_service.application.port.out.CustomerRepositoryPort;
import com.demo.customer_service.application.service.CustomerService;
import com.demo.customer_service.domain.model.Customer;
import com.demo.customer_service.domain.model.CustomerEmailAlreadyExistsException;
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

// CustomerService implements BOTH CreateCustomerUseCase and GetCustomerUseCase,
// so one test class covering both is appropriate here - we're testing the
// class, not the interfaces individually.
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    private CustomerService customerService;

    // --- createCustomer ---

    @Test
    void createCustomer_savesAndReturnsCustomer_whenEmailIsNew() {
        customerService = new CustomerService(customerRepository);
        var command = new CreateCustomerUseCase.Command("John", "Doe", "john.doe@example.com");

        when(customerRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        // save() just returns whatever entity is passed in, mirroring an in-memory repo
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.createCustomer(command);

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getId()).isNotNull(); // generated inside the Customer constructor
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_throws_whenEmailAlreadyExists() {
        customerService = new CustomerService(customerRepository);
        var command = new CreateCustomerUseCase.Command("John", "Doe", "john.doe@example.com");

        when(customerRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(command))
                .isInstanceOf(CustomerEmailAlreadyExistsException.class);

        // Critical assertion: save() must NEVER be called once the duplicate
        // check fails. If this fails, it means invalid data could still slip
        // into the database despite the business rule.
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // --- getAllCustomers ---

    @Test
    void getAllCustomers_returnsAllFromRepository() {
        customerService = new CustomerService(customerRepository);
        Customer c1 = new Customer("John", "Doe", "john@example.com");
        Customer c2 = new Customer("Jane", "Smith", "jane@example.com");
        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Customer::getFirstName).containsExactly("John", "Jane");
    }

    // --- getCustomerById ---

    @Test
    void getCustomerById_returnsCustomer_whenFound() {
        customerService = new CustomerService(customerRepository);
        UUID id = UUID.randomUUID();
        Customer customer = new Customer("John", "Doe", "john@example.com");
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(id);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getCustomerById_throws_whenNotFound() {
        customerService = new CustomerService(customerRepository);
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(id))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}