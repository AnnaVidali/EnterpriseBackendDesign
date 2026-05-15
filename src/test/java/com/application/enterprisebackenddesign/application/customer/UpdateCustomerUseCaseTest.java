package com.application.enterprisebackenddesign.application.customer;

import com.application.enterprisebackenddesign.api.customer.CustomerRequest;
import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private UpdateCustomerUseCase useCase;

    @Test
    void shouldUpdateAllFields() throws DomainException {
        Customer existing = new Customer(1L, "John", "Doe", "john@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        CustomerRequest request = new CustomerRequest("Jane", "Smith", "jane@example.com");
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.update(1L, request);

        assertThat(result.getName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        verify(eventPublisher, times(4)).publish(any());
    }

    @Test
    void shouldUpdateOnlyProvidedFields() throws DomainException {
        Customer existing = new Customer(1L, "John", "Doe", "john@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        CustomerRequest request = new CustomerRequest("Jane", null, null);
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.update(1L, request);

        assertThat(result.getName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(eventPublisher, times(2)).publish(any());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        CustomerRequest request = new CustomerRequest("Jane", "Smith", "jane@example.com");
        assertThatThrownBy(() -> useCase.update(99L, request))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
