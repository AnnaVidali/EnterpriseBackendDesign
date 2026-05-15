package com.application.enterprisebackenddesign.application.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCustomersUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private ListCustomersUseCase useCase;

    @Test
    void shouldReturnAllCustomers() throws DomainException {
        List<Customer> customers = List.of(
                new Customer(1L, "John", "Doe", "john@example.com"),
                new Customer(2L, "Jane", "Smith", "jane@example.com")
        );
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> result = useCase.listAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnPagedCustomers() throws DomainException {
        List<Customer> customers = List.of(new Customer(1L, "John", "Doe", "john@example.com"));
        Pageable pageable = PageRequest.of(0, 20);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(customers, pageable, 1));

        Page<Customer> result = useCase.listAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
