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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private CreateCustomerUseCase useCase;

    @Test
    void shouldCreateCustomerAndPublishEvents() throws DomainException {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com");
        Customer saved = new Customer(1L, "John", "Doe", "john@example.com");
        when(customerRepository.save(any())).thenReturn(saved);

        Customer result = useCase.create(1L, request);

        assertThat(result.getName()).isEqualTo("John");
        verify(customerRepository).save(any());
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenValidationFails() {
        CustomerRequest request = new CustomerRequest("", "Doe", "john@example.com");
        assertThatThrownBy(() -> useCase.create(1L, request))
                .isInstanceOf(DomainException.class);
        verifyNoInteractions(customerRepository, eventPublisher);
    }
}
