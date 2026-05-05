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
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    public Customer create(Long id, CustomerRequest request) throws DomainException {
        Customer customer = new Customer(id, request.name(), request.lastName(), request.email());
        Customer savedCustomer = customerRepository.save(customer);

        customer.pullEvents(true).forEach(eventPublisher::publish);

        return savedCustomer;
    }
}
