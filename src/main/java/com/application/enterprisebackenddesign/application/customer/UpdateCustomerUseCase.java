package com.application.enterprisebackenddesign.application.customer;

import com.application.enterprisebackenddesign.api.customer.CustomerRequest;
import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    public Customer update(Long id, CustomerRequest customerRequest) throws DomainException {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new DomainException.ResourceNotFoundException("Customer not found."));
        if (customerRequest.name() != null && !customerRequest.name().isEmpty()) {
            customer.updateName(customerRequest.name());
        }
        if (customerRequest.lastName() != null && !customerRequest.lastName().isEmpty()) {
            customer.updateLastName(customerRequest.lastName());
        }
        if (customerRequest.email() != null && !customerRequest.email().isEmpty()) {
            customer.updateEmail(customerRequest.email());
        }
        Customer updatedCustomer = customerRepository.save(customer);
        customer.pullEvents(true).forEach(eventPublisher::publish);
        return updatedCustomer;
    }
}
