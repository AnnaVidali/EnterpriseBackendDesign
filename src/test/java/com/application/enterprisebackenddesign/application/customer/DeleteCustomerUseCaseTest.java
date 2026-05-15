package com.application.enterprisebackenddesign.application.customer;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private DeleteCustomerUseCase useCase;

    @Test
    void shouldDeleteCustomerAndPublishEvents() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        useCase.delete(1L);

        verify(customerRepository).deleteById(1L);
        verify(eventPublisher, times(2)).publish(any());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.delete(99L))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verify(customerRepository, never()).deleteById(any());
        verifyNoInteractions(eventPublisher);
    }
}
