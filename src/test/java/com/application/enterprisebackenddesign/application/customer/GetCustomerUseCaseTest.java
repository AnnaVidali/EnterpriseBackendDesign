package com.application.enterprisebackenddesign.application.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private GetCustomerUseCase useCase;

    @Test
    void shouldReturnCustomerWhenFound() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = useCase.getById(1L);

        assertThat(result).isEqualTo(customer);
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.getById(99L))
                .isInstanceOf(DomainException.ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }
}
